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
package org.sonar.plugins.buildstability.ci.api;

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
   * True, if build unstable.
   */
  private Status status;

  /**
   * Build duration in milliseconds.
   */
  private long duration;

  /**
   * Build timestamp.
   */
  private long timestamp;

  public Build(int number, long timestamp, Status status, long duration) {
    this.number = number;
    this.timestamp = timestamp;
    this.status = status;
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

  public void setStatus(Status status) {
    this.status = status;
  }

  public Status getStatus() {
    return status;
  }

  public long getDuration() {
    return duration;
  }

  public void setDuration(long duration) {
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
        append("status", status).
        append("duration", duration).
        toString();
  }
}
