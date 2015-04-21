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
package org.sonar.plugins.buildstability.ci.teamcity;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.DefaultHttpClient;
import org.sonar.plugins.buildstability.ci.api.AbstractServer;
import org.sonar.plugins.buildstability.ci.api.Build;
import org.sonar.plugins.buildstability.ci.api.Unmarshaller;

import java.io.IOException;

/**
 * See <a href="http://confluence.jetbrains.com/display/TW/REST+API+Plugin#RESTAPIPlugin-Usage">TeamCity REST APIs</a>.
 *
 * @author Alexei Guevara <alguevara@kijiji.ca>
 */
public class TeamCityServer extends AbstractServer {
  public static final String SYSTEM = "TeamCity";
  public static final String PATTERN = "/viewType.html?buildTypeId=";
  private static final Unmarshaller<Build> BUILD_UNMARSHALLER = new TeamCityBuildUnmarshaller();

  @Override
  public String getBuildUrl(String number) {
    StringBuilder sb = new StringBuilder(getHost())
      .append("/httpAuth/app/rest/buildTypes/id:").append(getKey()).append("/builds/number:").append(number);
    return sb.toString();
  }

  @Override
  public String getLastBuildUrl() {
    StringBuilder sb = new StringBuilder(getHost())
      .append("/httpAuth/app/rest/buildTypes/id:").append(getKey()).append("/builds/count:0");
    return sb.toString();
  }

  @Override
  public Unmarshaller<Build> getBuildUnmarshaller() {
    return BUILD_UNMARSHALLER;
  }

  @Override
  public void doLogin(DefaultHttpClient client) throws IOException {
    Credentials credentials = new UsernamePasswordCredentials(getUsername(), getPassword());
    client.getCredentialsProvider().setCredentials(AuthScope.ANY, credentials);
  }
}
