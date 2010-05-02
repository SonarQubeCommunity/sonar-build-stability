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

package org.sonar.plugins.buildstability.ci;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Date;

/**
 * @author Evgeny Mandrikov
 */
public class Build implements Model {
  /**
   * Build number.
   */
  private int number;

  /**
   * Build result.
   */
  private String result;

  /**
   * True, if build successfull.
   */
  private boolean successful;

  /**
   * Build duration in millisec.
   */
  private double duration;

  /**
   * Build timestamp.
   */
  private long timestamp;

  public Build(int number, long timestamp, String result, boolean successful, double duration) {
    this.number = number;
    this.timestamp = timestamp;
    this.result = result;
    this.successful = successful;
    this.duration = duration;
  }

  public Build() {
  }

  public int getNumber() {
    return number;
  }

  public void setNumber(int number) {
    this.number = number;
  }

  public String getResult() {
    return result;
  }

  public void setResult(String result) {
    this.result = result;
  }

  public boolean isSuccessful() {
    return successful;
  }

  public void setSuccessful(boolean successful) {
    this.successful = successful;
  }

  public double getDuration() {
    return duration;
  }

  public void setDuration(double duration) {
    this.duration = duration;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public Date getDate() {
    return new Date(timestamp);
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this).
        append("number", number).
        append("timestamp", timestamp).
        append("result", result).
        append("successful", successful).
        append("duration", duration).
        toString();
  }
}
