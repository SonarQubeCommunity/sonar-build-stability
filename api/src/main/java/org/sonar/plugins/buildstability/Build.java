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
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Date;

/**
 * TODO comment me
 *
 * @author Evgeny Mandrikov
 */
public class Build {
  public static final String URL_FIELD = "url";
  public static final String NUMBER_FIELD = "num";
  public static final String TIMESTAMP_FIELD = "time";
  public static final String DURATION_FIELD = "duration";
  public static final String STATUS_FIELD = "res";
  public static final String DEVELOPERS_FIELD = "dev";

  public static final String SUCCESSFUL_STATUS = "ok";
  public static final String FAILED_STATUS = "fail";

  /**
   * Build URL.
   */
  private String url;

  /**
   * Build number. Required for metrics calculation.
   */
  private Integer number;

  /**
   * Build timestamp. Required for metrics calculation.
   */
  private Long timestamp;

  /**
   * Build result.
   */
  private String result;

  /**
   * True, if build successfull. Required for metrics calculation.
   */
  private Boolean successful;

  /**
   * Build duration in millisec. Required for metrics calculation.
   * TODO we really need value in milliseconds?
   */
  private Double duration;

  /**
   * TODO comment me, see SONARPLUGINS-482
   */
  private String[] developers;

  public Build(int number, long timestamp, String result, boolean successful, double duration) {
    this.number = number;
    this.timestamp = timestamp;
    this.result = result;
    this.successful = successful;
    this.duration = duration;
  }

  public Build() {
  }

  public String getUrl() {
    return url;
  }

  public Build setUrl(String url) {
    this.url = url;
    return this;
  }

  public int getNumber() {
    return number;
  }

  public Build setNumber(int number) {
    this.number = number;
    return this;
  }

  public String getResult() {
    return result;
  }

  public Build setResult(String result) {
    this.result = result;
    return this;
  }

  public boolean isSuccessful() {
    return successful;
  }

  public Build setSuccessful(boolean successful) {
    this.successful = successful;
    return this;
  }

  public double getDuration() {
    return duration;
  }

  public Build setDuration(double duration) {
    this.duration = duration;
    return this;
  }

  public long getDurationInSeconds() {
    return (long) (duration / 1000);
  }

  public Build setDurationInSeconds(long duration) {
    setDuration(duration * 1000);
    return this;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public Date getDate() {
    return new Date(timestamp);
  }

  public Build setTimestamp(long timestamp) {
    this.timestamp = timestamp;
    return this;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this).
        append(NUMBER_FIELD, number).
        append(TIMESTAMP_FIELD, timestamp).
        append(STATUS_FIELD, result).
        append("successful", successful).
        append(DURATION_FIELD, duration).
        toString();
  }

  public static Build fromString(String data) {
    Build build = new Build();
    String[] fields = StringUtils.split(data, ';');
    for (String field : fields) {
      String key = StringUtils.substringBefore(field, "=");
      String value = StringUtils.substringAfter(field, "=");
      if (URL_FIELD.equalsIgnoreCase(key)) {
        // TODO
      } else if (NUMBER_FIELD.equalsIgnoreCase(key)) {
        build.setNumber(Integer.parseInt(value));
      } else if (TIMESTAMP_FIELD.equalsIgnoreCase(key)) {
        build.setTimestamp(Long.parseLong(value));
      } else if (DURATION_FIELD.equalsIgnoreCase(key)) {
        build.setDurationInSeconds(Long.parseLong(value));
      } else if (STATUS_FIELD.equalsIgnoreCase(key)) {
        build.setResult(value);
        build.setSuccessful(Build.SUCCESSFUL_STATUS.equalsIgnoreCase(value));
      } else if (DEVELOPERS_FIELD.equalsIgnoreCase(key)) {
        // TODO
      }
    }
    if (build.number == null || build.timestamp == null || build.duration == null || build.successful == null) {
      // TODO error
    }
    return build;
  }

  public String convertToString() {
    StringBuilder sb = new StringBuilder();
    append(sb, URL_FIELD, url); // TODO escape
    append(sb, NUMBER_FIELD, number);
    append(sb, TIMESTAMP_FIELD, timestamp);
    append(sb, DURATION_FIELD, getDurationInSeconds());
    append(sb, STATUS_FIELD, successful ? SUCCESSFUL_STATUS : FAILED_STATUS);
    append(sb, DEVELOPERS_FIELD, StringUtils.join(developers, ','));
    return sb.toString();
  }

  private void append(StringBuilder sb, String field, Object value) {
    if (value == null) {
      return;
    }
    sb.append(field).append('=').append(value).append(';');
  }
}
