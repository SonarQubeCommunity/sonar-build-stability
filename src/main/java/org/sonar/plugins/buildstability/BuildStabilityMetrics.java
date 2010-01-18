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

import org.sonar.api.measures.Metric;
import org.sonar.api.measures.Metrics;

import java.util.Arrays;
import java.util.List;

/**
 * TODO:
 * Average time to fix a failure
 *
 * @author Evgeny Mandrikov
 */
public class BuildStabilityMetrics implements Metrics {
  public static final String DOMAIN_BUILD = "Build";

  /**
   * Number of successful builds.
   */
  public static final Metric SUCCESSFUL = new Metric(
      "successful",
      "Successful Builds",
      "Successful Builds",
      Metric.ValueType.INT,
      Metric.DIRECTION_BETTER,
      false,
      DOMAIN_BUILD
  );

  /**
   * Number of failed builds.
   */
  public static final Metric FAILED = new Metric(
      "failed",
      "Failed Builds",
      "Failed Builds",
      Metric.ValueType.INT,
      Metric.DIRECTION_WORST,
      false,
      DOMAIN_BUILD
  );

  /**
   * Success rate of the CI build of the project on Hudson (measured as
   * percentage of successful Hudson builds out of last 10 build)
   */
  public static final Metric SUCCESS_RATE = new Metric(
      "success_rate", // key
      "Success Rate", // name
      "Success Rate", // description
      Metric.ValueType.PERCENT,
      Metric.DIRECTION_BETTER,
      false,
      DOMAIN_BUILD
  );

  /**
   * Build average duration.
   */
  public static final Metric AVG_DURATION = new Metric(
      "avg_duration",
      "Avg Duration",
      "Avg Duration",
      Metric.ValueType.MILLISEC,
      Metric.DIRECTION_WORST,
      false,
      DOMAIN_BUILD
  );

  public List<Metric> getMetrics() {
    return Arrays.asList(
        SUCCESSFUL,
        FAILED,
        SUCCESS_RATE,
        AVG_DURATION
    );
  }
}
