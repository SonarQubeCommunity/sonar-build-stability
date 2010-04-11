/*
 * Copyright (C) 2010 Evgeny Mandrikov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sonar.plugins.buildstability;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.model.CiManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.PropertiesBuilder;
import org.sonar.api.resources.Project;
import org.sonar.plugins.buildstability.ci.Build;
import org.sonar.plugins.buildstability.ci.CiConnector;
import org.sonar.plugins.buildstability.ci.CiFactory;

import java.util.*;

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

  public boolean shouldExecuteOnProject(Project project) {
    return project.isRoot() &&
        StringUtils.isNotEmpty(getCiUrl(project));
  }

  protected String getCiUrl(Project project) {
    String url = project.getConfiguration().getString(CI_URL_PROPERTY);
    if (StringUtils.isNotEmpty(url)) {
      return url;
    }
    CiManagement ci = project.getPom().getCiManagement();
    if (ci != null && StringUtils.isNotEmpty(ci.getSystem()) && StringUtils.isNotEmpty(ci.getUrl())) {
      return ci.getSystem() + ":" + ci.getUrl();
    }
    return null;
  }

  public void analyse(Project project, SensorContext context) {
    Logger logger = LoggerFactory.getLogger(getClass());
    String ciUrl = getCiUrl(project);
    logger.info("CI URL: {}", ciUrl);
    String username = project.getConfiguration().getString(USERNAME_PROPERTY);
    String password = project.getConfiguration().getString(PASSWORD_PROPERTY);
    boolean useJSecurityCheck = project.getConfiguration().getBoolean(USE_JSECURITYCHECK_PROPERTY, USE_JSECURITYCHECK_DEFAULT_VALUE);
    List<Build> builds;
    try {
      CiConnector connector = CiFactory.create(ciUrl, username, password, useJSecurityCheck);
      if (connector == null) {
        logger.warn("Unknown CiManagement system or incorrect URL: {}", ciUrl);
        return;
      }
      int daysToRetrieve = project.getConfiguration().getInt(DAYS_PROPERTY, DAYS_DEFAULT_VALUE);
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

    if (!builds.isEmpty()) {
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
