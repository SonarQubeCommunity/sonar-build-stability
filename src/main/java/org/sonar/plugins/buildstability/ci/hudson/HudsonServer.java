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
package org.sonar.plugins.buildstability.ci.hudson;

import org.apache.commons.lang.StringUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.sonar.plugins.buildstability.ci.AbstractServer;
import org.sonar.plugins.buildstability.ci.Build;
import org.sonar.plugins.buildstability.ci.Unmarshaller;

import java.io.IOException;

/**
 * See <a href="http://wiki.hudson-ci.org/display/HUDSON/Remote+access+API">Hudson Remote access API</a>.
 *
 * @author Evgeny Mandrikov
 */
public class HudsonServer extends AbstractServer {
  public static final String SYSTEM = "Hudson";
  public static final String SYSTEM_JENKINS = "Jenkins";
  public static final String PATTERN = "/job/";

  private static final Unmarshaller<Build> BUILD_UNMARSHALLER = new HudsonBuildUnmarshaller();

  private boolean useJSecurityCheck;

  @Override
  protected String getBuildUrl(String number) {
    return getHost() + "/job/" + getKey() + "/" + number + "/api/xml/";
  }

  @Override
  protected String getLastBuildUrl() {
    return getBuildUrl("lastBuild");
  }

  @Override
  protected Unmarshaller<Build> getBuildUnmarshaller() {
    return BUILD_UNMARSHALLER;
  }

  @Override
  protected void doLogin(DefaultHttpClient client) throws IOException {
    if (!isUseJSecurityCheck()) {
      super.doLogin(client);
      return;
    }
    if (!StringUtils.isBlank(getUsername()) && !StringUtils.isBlank(getPassword())) {
      HudsonUtils.doLogin(client, getHost(), getUsername(), getPassword());
    }
  }

  public boolean isUseJSecurityCheck() {
    return useJSecurityCheck;
  }

  public void setUseJSecurityCheck(boolean useJSecurityCheck) {
    this.useJSecurityCheck = useJSecurityCheck;
  }
}
