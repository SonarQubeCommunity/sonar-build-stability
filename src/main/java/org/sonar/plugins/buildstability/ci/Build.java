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

/**
 * @author Evgeny Mandrikov
 */
public class Build {
  private final int number;
  private final String result;
  private final boolean successfull;
  private final double duration;

  /**
   * @param number      build number
   * @param result      build result
   * @param successfull true, if build successfulls
   * @param duration    build duration in millisec
   */
  public Build(int number, String result, boolean successfull, double duration) {
    this.number = number;
    this.result = result;
    this.successfull = successfull;
    this.duration = duration;
  }

  public int getNumber() {
    return number;
  }

  public String getResult() {
    return result;
  }

  public boolean isSuccessfull() {
    return successfull;
  }

  public double getDuration() {
    return duration;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this).
        append("number", number).
        append("result", result).
        append("successfull", successfull).
        append("duration", duration).
        toString();
  }
}
