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

import org.sonar.api.measures.Metric;
import org.sonar.api.measures.Metrics;

import java.util.Arrays;
import java.util.List;

/**
 * @author Evgeny Mandrikov
 */
public class BuildStabilityMetrics implements Metrics {
  public static final String DOMAIN_BUILD = "Continuous integration";

  /**
   * Number of builds.
   */
  public static final Metric BUILDS = new Metric(
      "builds",
      "Builds",
      "Number of builds",
      Metric.ValueType.INT,
      Metric.DIRECTION_NONE,
      false,
      DOMAIN_BUILD
  );

  /**
   * Number of failed builds.
   */
  public static final Metric FAILED = new Metric(
      "build_failures",
      "Failed Builds",
      "Number of failed builds",
      Metric.ValueType.INT,
      Metric.DIRECTION_WORST,
      false,
      DOMAIN_BUILD
  );

  /**
   * Ratio of successful builds. Measured as percentage of successful builds out of all last builds.
   */
  public static final Metric SUCCESS_RATE = new Metric(
      "build_success_density",
      "Success Rate (%)",
      "Ratio of successful builds",
      Metric.ValueType.PERCENT,
      Metric.DIRECTION_BETTER,
      false,
      DOMAIN_BUILD
  );

  /**
   * Build average duration. Includes only duration of successful builds.
   */
  public static final Metric AVG_DURATION = new Metric(
      "build_average_duration",
      "Average Duration",
      "Average Duration",
      Metric.ValueType.MILLISEC,
      Metric.DIRECTION_WORST,
      false,
      DOMAIN_BUILD
  );

  /**
   * Duration of longest successful build.
   */
  public static final Metric LONGEST_DURATION = new Metric(
      "build_longest_duration",
      "Longest duration",
      "Duration of longest successful build",
      Metric.ValueType.MILLISEC,
      Metric.DIRECTION_WORST,
      false,
      DOMAIN_BUILD
  );

  /**
   * Duration of shortest successful build.
   */
  public static final Metric SHORTEST_DURATION = new Metric(
      "build_shortest_duration",
      "Shortest duration",
      "Duration of shortest successful build",
      Metric.ValueType.MILLISEC,
      Metric.DIRECTION_WORST,
      false,
      DOMAIN_BUILD
  );

  /**
   * Average time to fix a failure.
   */
  public static final Metric AVG_TIME_TO_FIX = new Metric(
      "build_average_time_to_fix_failure",
      "Average time to fix a failure",
      "Average time to fix a failure",
      Metric.ValueType.MILLISEC,
      Metric.DIRECTION_WORST,
      false,
      DOMAIN_BUILD
  );

  /**
   * Longest time to fix a failure.
   */
  public static final Metric LONGEST_TIME_TO_FIX = new Metric(
      "build_longest_time_to_fix_failure",
      "Longest time to fix a failure",
      "Longest time to fix a failure",
      Metric.ValueType.MILLISEC,
      Metric.DIRECTION_WORST,
      false,
      DOMAIN_BUILD
  );

  /**
   * Average number of builds between fixes.
   */
  public static final Metric AVG_BUILDS_TO_FIX = new Metric(
      "build_average_builds_to_fix_failure",
      "Average number of builds between fixes",
      "Average number of builds between fixes",
      Metric.ValueType.INT,
      Metric.DIRECTION_WORST,
      false,
      DOMAIN_BUILD
  );

  /**
   * TODO comment me (seconds)
   */
  public static final Metric DURATIONS = new Metric(
      "build_durations",
      "Durations",
      "Durations",
      Metric.ValueType.DATA,
      Metric.DIRECTION_NONE,
      false,
      DOMAIN_BUILD
  );

  /**
   * TODO comment me
   */
  public static final Metric RESULTS = new Metric(
      "build_results",
      "Results",
      "Results",
      Metric.ValueType.DATA,
      Metric.DIRECTION_NONE,
      false,
      DOMAIN_BUILD
  );

  public List<Metric> getMetrics() {
    return Arrays.asList(
        BUILDS,
        FAILED,
        SUCCESS_RATE,

        AVG_DURATION,
        LONGEST_DURATION,
        SHORTEST_DURATION,

        AVG_TIME_TO_FIX,
        LONGEST_TIME_TO_FIX,
        AVG_BUILDS_TO_FIX,

        DURATIONS,
        RESULTS
    );
  }
}
