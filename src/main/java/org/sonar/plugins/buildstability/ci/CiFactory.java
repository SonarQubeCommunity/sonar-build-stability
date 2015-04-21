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
package org.sonar.plugins.buildstability.ci;

import org.sonar.plugins.buildstability.ci.api.AbstractServer;
import org.apache.commons.lang.StringUtils;
import org.sonar.plugins.buildstability.ci.bamboo.BambooServer;
import org.sonar.plugins.buildstability.ci.hudson.HudsonServer;
import org.sonar.plugins.buildstability.ci.teamcity.TeamCityServer;

/**
 * @author Evgeny Mandrikov
 */
public final class CiFactory {
  /**
   * Hide utility-class constructor.
   */
  private CiFactory() {
  }

  public static String getSystem(String ciUrl) {
    return StringUtils.substringBefore(ciUrl, ":");
  }

  public static String getUrl(String ciUrl) {
    return StringUtils.substringAfter(ciUrl, ":");
  }

  public static CiConnector create(String ciUrl, String username, String password, boolean useJSecurityCheck) {
    return create(getSystem(ciUrl), getUrl(ciUrl), username, password, useJSecurityCheck);
  }

  public static CiConnector create(String system, String url, String username, String password, boolean useJSecurityCheck) {
    AbstractServer server;
    String pattern;
    if (BambooServer.SYSTEM.equalsIgnoreCase(system)) {
      server = new BambooServer();
      pattern = BambooServer.PATTERN;
    } else if (HudsonServer.SYSTEM.equalsIgnoreCase(system) || HudsonServer.SYSTEM_JENKINS.equalsIgnoreCase(system)) {
      server = new HudsonServer();
      ((HudsonServer) server).setUseJSecurityCheck(useJSecurityCheck);
      pattern = HudsonServer.PATTERN;
    } else if (TeamCityServer.SYSTEM.equalsIgnoreCase(system)) {
    	server = new TeamCityServer();
    	pattern = TeamCityServer.PATTERN;
    } else {
      return null;
    }
    server.setUsername(username);
    server.setPassword(password);

    int i = url.indexOf(pattern);
    if (i == -1) {
      return null;
    }
    String base = url.substring(0, i);
    String key = url.substring(i + pattern.length());
    server.setHost(base);
    server.setKey(key);

    return new CiConnector(server);
  }
}
