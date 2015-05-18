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

import java.io.IOException;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.client.HttpClient;

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

  /**
   * Return a non <code>null</code> URL when this servers supports to retrieve all builds after the given date.
   * 
   * @param date
   *          the minimal date.
   * @return <code>null</code> when unsupported, and a valid URL otherwise.
   */
  public String getBuildsSinceUrl(final Date date) {
    // As default, this is not supported
    return null;
  }

  /**
   * Return a non <code>null</code> URL when this servers supports to retrieve the last builds with a given limit.
   * 
   * @param count
   *          the maximal amount of builds to return.
   * @return <code>null</code> when unsupported, and a valid URL otherwise.
   */
  public String getBuildsUrl(final int count) {
    // As default, this is not supported
    return null;
  }

  /**
   * Proceed to login.
   * 
   * @param client
   *          HTTP client used to proceed the login.
   */
  public void doLogin(HttpClient client) throws IOException {
    if (isAuthenticatedLogin()) {
      ((DefaultHttpClient)client).getCredentialsProvider().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(getUsername(), getPassword()));
    }
  }

  /**
   * Indicate the authentication information is provided.
   * 
   * @return <code>true</code> when user and password are provided.
   */
  public boolean isAuthenticatedLogin() {
    return StringUtils.isNotBlank(getUsername()) && StringUtils.isNotBlank(getPassword());
  }

}
