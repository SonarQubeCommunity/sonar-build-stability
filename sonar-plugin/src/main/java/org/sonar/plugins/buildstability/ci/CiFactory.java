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
package org.sonar.plugins.buildstability.ci;

import org.apache.commons.lang.StringUtils;
import org.sonar.plugins.buildstability.ci.bamboo.BambooServer;
import org.sonar.plugins.buildstability.ci.hudson.HudsonServer;

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
    } else if (HudsonServer.SYSTEM.equalsIgnoreCase(system)) {
      server = new HudsonServer();
      ((HudsonServer) server).setUseJSecurityCheck(useJSecurityCheck);
      pattern = HudsonServer.PATTERN;
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
