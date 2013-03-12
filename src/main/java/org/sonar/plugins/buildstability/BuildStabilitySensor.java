/*
 * Sonar Build Stability Plugin
 * Copyright (C) 2010 SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.buildstability;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.PropertiesBuilder;
import org.sonar.api.resources.Project;
import org.sonar.plugins.buildstability.ci.Build;
import org.sonar.plugins.buildstability.ci.CiConnector;
import org.sonar.plugins.buildstability.ci.CiFactory;
import org.sonar.plugins.buildstability.ci.MavenCiConfiguration;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * @author Evgeny Mandrikov
 */
public class BuildStabilitySensor implements Sensor {
  public static final String DAYS_PROPERTY = "sonar.build-stability.days";
  public static final int DAYS_DEFAULT_VALUE = 30;
  public static final String USERNAME_PROPERTY = "sonar.build-stability.username.secured";
  public static final String PASSWORD_PROPERTY = "sonar.build-stability.password.secured";
  public static final String USE_JSECURITYCHECK_PROPERTY = "sonar.build-stability.use_jsecuritycheck";
  public static final boolean USE_JSECURITYCHECK_DEFAULT_VALUE = false;
  public static final String CI_URL_PROPERTY = "sonar.build-stability.url";

  private final Settings settings;
  private final MavenCiConfiguration mavenCiConfiguration;

  public BuildStabilitySensor(Settings settings, MavenCiConfiguration mavenCiConfiguration) {
    this.settings = settings;
    this.mavenCiConfiguration = mavenCiConfiguration;
  }

  /**
   * In case we are not in a Maven build this constructor will be called
   */
  public BuildStabilitySensor(Settings settings) {
    this(settings, null /* Not in a Maven build */);
  }

  public boolean shouldExecuteOnProject(Project project) {
    return project.isRoot() &&
      StringUtils.isNotEmpty(getCiUrl(project));
  }

  protected String getCiUrl(Project project) {
    String url = settings.getString(CI_URL_PROPERTY);
    if (StringUtils.isNotEmpty(url)) {
      return url;
    }
    if (mavenCiConfiguration != null) {
      if (StringUtils.isNotEmpty(mavenCiConfiguration.getSystem()) && StringUtils.isNotEmpty(mavenCiConfiguration.getUrl())) {
        return mavenCiConfiguration.getSystem() + ":" + mavenCiConfiguration.getUrl();
      }
    }
    return null;
  }

  public void analyse(Project project, SensorContext context) {
    Logger logger = LoggerFactory.getLogger(getClass());
    String ciUrl = getCiUrl(project);
    logger.info("CI URL: {}", ciUrl);
    String username = settings.getString(USERNAME_PROPERTY);
    String password = settings.getString(PASSWORD_PROPERTY);
    boolean useJSecurityCheck = settings.getBoolean(USE_JSECURITYCHECK_PROPERTY);
    List<Build> builds;
    try {
      CiConnector connector = CiFactory.create(ciUrl, username, password, useJSecurityCheck);
      if (connector == null) {
        logger.warn("Unknown CiManagement system or incorrect URL: {}", ciUrl);
        return;
      }
      int daysToRetrieve = settings.getInt(DAYS_PROPERTY);
      Calendar calendar = Calendar.getInstance();
      calendar.add(Calendar.DAY_OF_MONTH, -daysToRetrieve);
      Date date = calendar.getTime();
      builds = connector.getBuildsSince(date);
      logger.info("Retrieved {} builds since {}", builds.size(), date);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      return;
    }
    analyseBuilds(builds, context);
  }

  protected void analyseBuilds(List<Build> builds, SensorContext context) {
    Logger logger = LoggerFactory.getLogger(getClass());

    Collections.sort(builds, new Comparator<Build>() {
      public int compare(Build o1, Build o2) {
        return o1.getNumber() - o2.getNumber();
      }
    });

    PropertiesBuilder<Integer, Double> durationsBuilder = new PropertiesBuilder<Integer, Double>(BuildStabilityMetrics.DURATIONS);
    PropertiesBuilder<Integer, String> resultsBuilder = new PropertiesBuilder<Integer, String>(BuildStabilityMetrics.RESULTS);

    double successful = 0;
    double failed = 0;
    double duration = 0;
    double shortest = Double.POSITIVE_INFINITY;
    double longest = Double.NEGATIVE_INFINITY;

    double totalTimeToFix = 0;
    double totalBuildsToFix = 0;
    double longestTimeToFix = Double.NEGATIVE_INFINITY;
    int fixes = 0;
    Build firstFailed = null;

    for (Build build : builds) {
      logger.debug(build.toString());

      int buildNumber = build.getNumber();
      double buildDuration = build.getDuration();
      resultsBuilder.add(buildNumber, build.isSuccessful() ? "g" : "r");
      durationsBuilder.add(buildNumber, buildDuration / 1000);
      if (build.isSuccessful()) {
        successful++;
        duration += buildDuration;
        shortest = Math.min(shortest, buildDuration);
        longest = Math.max(longest, buildDuration);
        if (firstFailed != null) {
          // Build fixed
          long buildsToFix = build.getNumber() - firstFailed.getNumber();
          totalBuildsToFix += buildsToFix;
          double timeToFix = build.getTimestamp() - firstFailed.getTimestamp();
          totalTimeToFix += timeToFix;
          longestTimeToFix = Math.max(longestTimeToFix, timeToFix);
          fixes++;
          firstFailed = null;
        }
      } else {
        failed++;
        if (firstFailed == null) {
          // Build failed
          firstFailed = build;
        }
      }
    }

    double count = successful + failed;

    context.saveMeasure(new Measure(BuildStabilityMetrics.BUILDS, count));
    context.saveMeasure(new Measure(BuildStabilityMetrics.FAILED, failed));
    context.saveMeasure(new Measure(BuildStabilityMetrics.SUCCESS_RATE, divide(successful, count) * 100));

    context.saveMeasure(new Measure(BuildStabilityMetrics.AVG_DURATION, divide(duration, successful)));
    context.saveMeasure(new Measure(BuildStabilityMetrics.LONGEST_DURATION, normalize(longest)));
    context.saveMeasure(new Measure(BuildStabilityMetrics.SHORTEST_DURATION, normalize(shortest)));

    context.saveMeasure(new Measure(BuildStabilityMetrics.AVG_TIME_TO_FIX, divide(totalTimeToFix, fixes)));
    context.saveMeasure(new Measure(BuildStabilityMetrics.LONGEST_TIME_TO_FIX, normalize(longestTimeToFix)));
    context.saveMeasure(new Measure(BuildStabilityMetrics.AVG_BUILDS_TO_FIX, divide(totalBuildsToFix, fixes)));

    if (builds.size() > 0) {
      context.saveMeasure(durationsBuilder.build());
      context.saveMeasure(resultsBuilder.build());
    }
  }

  private double normalize(double value) {
    return Double.isInfinite(value) ? 0 : value;
  }

  private double divide(double v1, double v2) {
    return v2 == 0 ? 0 : v1 / v2;
  }
}
