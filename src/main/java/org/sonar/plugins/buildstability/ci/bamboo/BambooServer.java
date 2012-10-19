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

package org.sonar.plugins.buildstability.ci.bamboo;

import org.sonar.plugins.buildstability.ci.AbstractServer;
import org.sonar.plugins.buildstability.ci.Build;
import org.sonar.plugins.buildstability.ci.Unmarshaller;

/**
 * See <a href="http://confluence.atlassian.com/display/BAMBOO/Bamboo+REST+APIs">Bamboo REST APIs</a>.
 *
 * @author Evgeny Mandrikov
 */
public class BambooServer extends AbstractServer {
  public static final String SYSTEM = "Bamboo";
  public static final String PATTERN = "/browse/";
  private static final Unmarshaller<Build> BUILD_UNMARSHALLER = new BambooBuildUnmarshaller();

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
  protected Unmarshaller<Build> getBuildUnmarshaller() {
    return BUILD_UNMARSHALLER;
  }
}
