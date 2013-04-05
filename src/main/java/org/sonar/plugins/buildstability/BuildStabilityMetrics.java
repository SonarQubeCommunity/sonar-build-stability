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
  public static final Metric BUILDS = new Metric.Builder("builds", "Builds", Metric.ValueType.INT)
      .setDescription("Number of builds")
      .setDirection(Metric.DIRECTION_NONE)
      .setQualitative(false)
      .setDomain(DOMAIN_BUILD)
      .create();

  /**
   * Number of failed builds.
   */
  public static final Metric FAILED = new Metric.Builder("build_failures", "Failed Builds", Metric.ValueType.INT)
      .setDescription("Number of failed builds")
      .setDirection(Metric.DIRECTION_WORST)
      .setQualitative(false)
      .setDomain(DOMAIN_BUILD)
      .create();

  /**
   * Ratio of successful builds. Measured as percentage of successful builds out of all last builds.
   */
  public static final Metric SUCCESS_RATE = new Metric.Builder("build_success_density", "Success Rate (%)",
      Metric.ValueType.PERCENT)
      .setDescription("Ratio of successful builds")
      .setDirection(Metric.DIRECTION_BETTER)
      .setQualitative(false)
      .setDomain(DOMAIN_BUILD)
      .create();

  /**
   * Build average duration. Includes only duration of successful builds.
   */
  public static final Metric AVG_DURATION = new Metric.Builder("build_average_duration", "Average Duration",
      Metric.ValueType.MILLISEC)
      .setDescription("Average Duration")
      .setDirection(Metric.DIRECTION_WORST)
      .setQualitative(false)
      .setDomain(DOMAIN_BUILD)
      .create();

  /**
   * Duration of longest successful build.
   */
  public static final Metric LONGEST_DURATION = new Metric.Builder("build_longest_duration", "Longest duration",
      Metric.ValueType.MILLISEC)
      .setDescription("Duration of longest successful build")
      .setDirection(Metric.DIRECTION_WORST)
      .setQualitative(false)
      .setDomain(DOMAIN_BUILD)
      .create();

  /**
   * Duration of shortest successful build.
   */
  public static final Metric SHORTEST_DURATION = new Metric.Builder("build_shortest_duration", "Shortest duration",
      Metric.ValueType.MILLISEC)
      .setDescription("Duration of shortest successful build")
      .setDirection(Metric.DIRECTION_WORST)
      .setQualitative(false)
      .setDomain(DOMAIN_BUILD)
      .create();

  /**
   * Average time to fix a failure.
   */
  public static final Metric AVG_TIME_TO_FIX = new Metric.Builder("build_average_time_to_fix_failure",
      "Average time to fix a failure",
      Metric.ValueType.MILLISEC)
      .setDescription("Average time to fix a failure")
      .setDirection(Metric.DIRECTION_WORST)
      .setQualitative(false)
      .setDomain(DOMAIN_BUILD)
      .create();

  /**
   * Longest time to fix a failure.
   */
  public static final Metric LONGEST_TIME_TO_FIX = new Metric.Builder("build_longest_time_to_fix_failure",
      "Longest time to fix a failure",
      Metric.ValueType.MILLISEC)
      .setDescription("Longest time to fix a failure")
      .setDirection(Metric.DIRECTION_WORST)
      .setQualitative(false)
      .setDomain(DOMAIN_BUILD)
      .create();

  /**
   * Average number of builds between fixes.
   */
  public static final Metric AVG_BUILDS_TO_FIX = new Metric.Builder("build_average_builds_to_fix_failure",
      "Average number of builds between fixes",
      Metric.ValueType.INT)
      .setDescription("Average number of builds between fixes")
      .setDirection(Metric.DIRECTION_WORST)
      .setQualitative(false)
      .setDomain(DOMAIN_BUILD)
      .create();

  public static final Metric DURATIONS = new Metric.Builder("build_durations", "Durations", Metric.ValueType.DATA)
      .setDescription("Durations")
      .setDirection(Metric.DIRECTION_NONE)
      .setQualitative(false)
      .setDomain(DOMAIN_BUILD)
      .create();

  public static final Metric RESULTS = new Metric.Builder("build_results", "Results", Metric.ValueType.DATA)
      .setDescription("Results")
      .setDirection(Metric.DIRECTION_NONE)
      .setQualitative(false)
      .setDomain(DOMAIN_BUILD)
      .create();

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
        RESULTS);
  }
}
