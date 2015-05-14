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
package org.sonar.plugins.buildstability.ci.jenkins;

import org.apache.commons.lang.StringUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.sonar.plugins.buildstability.ci.api.AbstractServer;
import org.sonar.plugins.buildstability.ci.api.Build;
import org.sonar.plugins.buildstability.ci.api.Unmarshaller;

import java.io.IOException;
import java.util.Date;

/**
 * See <a href="https://wiki.jenkins-ci.org/display/JENKINS/Remote+access+API">Jenkins Remote access API</a>.
 *
 * @author Evgeny Mandrikov
 */
public class JenkinsServer extends AbstractServer {
  public static final String SYSTEM = "Jenkins";
  public static final String PATTERN = "/job/";

  private static final Unmarshaller<Build> BUILD_UNMARSHALLER = new JenkinsBuildUnmarshaller();

  private boolean useJSecurityCheck;

  @Override
  public String getBuildUrl(String number) {
    return getHost() + "/job/" + getKey() + "/" + number + "/api/xml/";
  }

  @Override
  public String getBuildsSinceUrl(final Date date) {
    // This query gets the minimal data
    return getHost() + "/job/" + getKey() + "/api/xml?tree=builds[number,result,timestamp,duration]&xpath=//build[timestamp%20%3E%3D%20" + date.getTime() + "]&wrapper=builds";
  }

  @Override
  public String getBuildsUrl(final int count) {
    // This query gets the minimal data
    return getHost() + "/job/" + getKey() + "/api/xml?tree=builds[number,result,timestamp,duration]&xpath=//build[position()%20%3C%3D%20" + count + "]&wrapper=builds";
  }

  @Override
  public String getLastBuildUrl() {
    return getBuildUrl("lastBuild");
  }

  @Override
  public Unmarshaller<Build> getBuildUnmarshaller() {
    return BUILD_UNMARSHALLER;
  }

  @Override
  public void doLogin(DefaultHttpClient client) throws IOException {
    if (!isUseJSecurityCheck()) {
      super.doLogin(client);
      return;
    }
    if (!StringUtils.isBlank(getUsername()) && !StringUtils.isBlank(getPassword())) {
      JenkinsUtils.doLogin(client, getHost(), getUsername(), getPassword());
    }
  }

  public boolean isUseJSecurityCheck() {
    return useJSecurityCheck;
  }

  public void setUseJSecurityCheck(boolean useJSecurityCheck) {
    this.useJSecurityCheck = useJSecurityCheck;
  }
}
