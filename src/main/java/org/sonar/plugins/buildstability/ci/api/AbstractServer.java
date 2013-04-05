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

import org.apache.commons.lang.StringUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;

/**
 * @author Evgeny Mandrikov
 */
public abstract class AbstractServer {
  private String host;
  private String username;
  private String password;
  private String key;

  public AbstractServer() {
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public abstract String getBuildUrl(String number);

  public abstract String getLastBuildUrl();

  public abstract Unmarshaller<Build> getBuildUnmarshaller();

  public void doLogin(DefaultHttpClient client) throws IOException {
    if (StringUtils.isNotBlank(getUsername()) && StringUtils.isNotBlank(getPassword())) {
      client.getCredentialsProvider().setCredentials(
          AuthScope.ANY,
          new UsernamePasswordCredentials(getUsername(), getPassword()));
    }
  }
}
