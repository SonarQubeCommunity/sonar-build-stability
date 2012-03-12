/*
 * Sonar Build Stability Plugin
 * Copyright (C) 2010 Evgeny Mandrikov
 * dev@sonar.codehaus.org
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
package org.sonar.plugins.buildstability.ci.bamboo;

import org.sonar.plugins.buildstability.ci.AbstractServer;
import org.sonar.plugins.buildstability.ci.Unmarshaller;

/**
 * See <a href="http://confluence.atlassian.com/display/BAMBOO/Bamboo+REST+APIs">Bamboo REST APIs</a>.
 *
 * @author Evgeny Mandrikov
 */
public class BambooServer extends AbstractServer {
  public static final String SYSTEM = "Bamboo";
  public static final String PATTERN = "/browse/";
  private static final Unmarshaller BUILD_UNMARSHALLER = new BambooBuildUnmarshaller();

  protected String getBuildUrl(String number) {
    StringBuilder sb = new StringBuilder(getHost())
        .append("/rest/api/latest/build/").append(getKey()).append("/").append(number)
        .append("?os_authType=basic");
    return sb.toString();
  }

  @Override
  protected String getLastBuildUrl() {
    return getBuildUrl("latest");
  }

  @Override
  protected Unmarshaller getBuildUnmarshaller() {
    return BUILD_UNMARSHALLER;
  }
}
