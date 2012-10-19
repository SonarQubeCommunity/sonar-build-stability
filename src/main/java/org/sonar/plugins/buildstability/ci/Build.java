/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2010 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
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
