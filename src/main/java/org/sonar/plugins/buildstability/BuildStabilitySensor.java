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

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.TimeMachine;
import org.sonar.api.batch.TimeMachineQuery;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.Measure;
import org.sonar.api.resources.Project;
import org.sonar.plugins.buildstability.ci.CiConnector;
import org.sonar.plugins.buildstability.ci.CiFactory;
import org.sonar.plugins.buildstability.ci.MavenCiConfiguration;
import org.sonar.plugins.buildstability.ci.api.Build;
import org.sonar.plugins.buildstability.ci.api.Status;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

/**
 * @author Evgeny Mandrikov
 */
public class BuildStabilitySensor implements Sensor {
  private static final Logger LOG = LoggerFactory.getLogger(BuildStabilitySensor.class);

  public static final String DAYS_PROPERTY = "sonar.build-stability.days";
  public static final int DAYS_DEFAULT_VALUE = 30;
  public static final String USERNAME_PROPERTY = "sonar.build-stability.username.secured";

  @SuppressWarnings("squid:S2068")
  public static final String PASSWORD_PROPERTY = "sonar.build-stability.password.secured";
  public static final String USE_JSECURITYCHECK_PROPERTY = "sonar.build-stability.use_jsecuritycheck";
  public static final boolean USE_JSECURITYCHECK_DEFAULT_VALUE = false;
  public static final String CI_URL_PROPERTY = "sonar.build-stability.url";

  private final Settings settings;
  private final BuildAsString buildMetric = new BuildAsString();
  private final MavenCiConfiguration mavenCiConfiguration;

  private final TimeMachine timeMachine;

  public BuildStabilitySensor(Settings settings, TimeMachine timeMachine, @Nullable MavenCiConfiguration mavenCiConfiguration) {
    this.settings = settings;
    this.mavenCiConfiguration = mavenCiConfiguration;
    this.timeMachine = timeMachine;
  }

  /**
   * In case we are not in a Maven build this constructor will be called
   */
  public BuildStabilitySensor(Settings settings, TimeMachine timeMachine) {
    this(settings, timeMachine, null /* Not in a Maven build */);
  }

  @Override
  public boolean shouldExecuteOnProject(Project project) {
    return project.isRoot() && StringUtils.isNotEmpty(getCiUrl(project));
  }

  /**
   * Return the URL access to CI server. Either from settings either from the Maven configuration.
   */
  protected String getCiUrl(Project project) {
    String url = settings.getString(CI_URL_PROPERTY);
    if (StringUtils.isNotEmpty(url)) {
      return url;
    }
    if (mavenCiConfiguration != null && StringUtils.isNotEmpty(mavenCiConfiguration.getSystem()) && StringUtils.isNotEmpty(mavenCiConfiguration.getUrl())) {
      return mavenCiConfiguration.getSystem() + ":" + mavenCiConfiguration.getUrl();
    }
    return null;
  }

  @Override
  public void analyse(Project project, SensorContext context) {
    List<Build> builds;
    Date date;
    try {
      final String ciUrl = getCiUrl(project);
      final CiConnector connector = getConnector(ciUrl);
      if (connector == null) {
        LOG.warn("Unknown CiManagement system or incorrect URL: {}", ciUrl);
        return;
      }

      // Compute the epoch date
      final Calendar calendar = Calendar.getInstance();
      calendar.add(Calendar.DAY_OF_MONTH, -settings.getInt(DAYS_PROPERTY));
      date = calendar.getTime();
      builds = connector.getBuildsSince(date);
      LOG.info("Retrieved {} builds since {}", builds.size(), date);
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
      return;
    }

    analyseBuilds(completeAndPurgeBuilds(builds, date, project), context);
  }

  /**
   * Return the {@link Connector} to use for given URL.
   */
  protected CiConnector getConnector(final String ciUrl) {
    LOG.info("CI URL: {}", ciUrl);
    final String username = settings.getString(USERNAME_PROPERTY);
    final String password = settings.getString(PASSWORD_PROPERTY);
    final boolean useJSecurityCheck = settings.getBoolean(USE_JSECURITYCHECK_PROPERTY);
    return CiFactory.create(ciUrl, username, password, useJSecurityCheck);
  }

  /**
   * Complete the last analyzed builds with the discovered builds, and purge the builds out of retention delay.
   */
  protected Collection<Build> completeAndPurgeBuilds(final List<Build> builds, final Date epoch, Project project) {

    final TimeMachineQuery query = new TimeMachineQuery(project).setOnlyLastAnalysis(true).setMetrics(BuildStabilityMetrics.BUILDS_DETAILS);
    @SuppressWarnings("rawtypes")
    final List<Measure> measures = timeMachine.getMeasures(query);

    // Get the previous measure to complete
    final String buildsAsString;
    if (measures.isEmpty()) {
      buildsAsString = "";
      LOG.info("First analysis");
    } else {
      LOG.info("Merge with previous builds : {} ", measures.size());
      buildsAsString = (String) measures.get(0).getData();
    }

    final Map<Integer, Build> allBuilds = buildMetric.toBuilds(buildsAsString);

    // Complete the old build set with the new ones
    for (final Build newBuild : builds) {
      allBuilds.put(newBuild.getNumber(), newBuild);
    }

    // Purge the new set according the retention setting
    final Collection<Build> result = Maps.filterValues(allBuilds, new Predicate<Build>() {

      @Override
      public boolean apply(Build arg0) {
        return !arg0.getDate().before(epoch);
      }
    }).values();
    return result;
  }

  /**
   * Analyze the builds and save data.
   */
  protected void analyseBuilds(Collection<Build> builds, SensorContext context) {

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
      LOG.debug(build.toString());

      if (build.getStatus() == Status.failed) {
        failed++;
        if (firstFailed == null) {
          // Build failed
          firstFailed = build;
        }
      } else {
        successful++;
        double buildDuration = build.getDuration();
        duration += buildDuration;
        shortest = Math.min(shortest, buildDuration);
        longest = Math.max(longest, buildDuration);
        if (firstFailed != null) {
          // Build fixed
          long buildsToFix = ((long) build.getNumber()) - firstFailed.getNumber();
          totalBuildsToFix += buildsToFix;
          double timeToFix = build.getTimestamp() - firstFailed.getTimestamp();
          totalTimeToFix += timeToFix;
          longestTimeToFix = Math.max(longestTimeToFix, timeToFix);
          fixes++;
          firstFailed = null;
        }
      }
    }

    double count = successful + failed;

    context.saveMeasure(new Measure<Integer>(BuildStabilityMetrics.BUILDS, count));
    context.saveMeasure(new Measure<Integer>(BuildStabilityMetrics.FAILED, failed));
    context.saveMeasure(new Measure<Double>(BuildStabilityMetrics.SUCCESS_RATE, divide(successful, count) * 100));

    context.saveMeasure(new Measure<Integer>(BuildStabilityMetrics.AVG_DURATION, divide(duration, successful)));
    context.saveMeasure(new Measure<Integer>(BuildStabilityMetrics.LONGEST_DURATION, normalize(longest)));
    context.saveMeasure(new Measure<Integer>(BuildStabilityMetrics.SHORTEST_DURATION, normalize(shortest)));

    context.saveMeasure(new Measure<Integer>(BuildStabilityMetrics.AVG_TIME_TO_FIX, divide(totalTimeToFix, fixes)));
    context.saveMeasure(new Measure<Integer>(BuildStabilityMetrics.LONGEST_TIME_TO_FIX, normalize(longestTimeToFix)));
    context.saveMeasure(new Measure<Integer>(BuildStabilityMetrics.AVG_BUILDS_TO_FIX, divide(totalBuildsToFix, fixes)));

    if (!builds.isEmpty()) {
      context.saveMeasure(new Measure<String>(BuildStabilityMetrics.BUILDS_DETAILS, buildMetric.toString(builds)));
    }
  }

  private double normalize(double value) {
    return Double.isInfinite(value) ? 0 : value;
  }

  private double divide(double v1, double v2) {
    return Double.doubleToRawLongBits(v2) == 0 ? 0 : v1 / v2;
  }

  @Override
  public String toString() {
    return "Build Stability";
  }
}