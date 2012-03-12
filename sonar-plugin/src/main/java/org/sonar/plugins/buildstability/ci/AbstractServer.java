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

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.lang.StringUtils;

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

  protected abstract String getBuildUrl(String number);

  protected abstract String getLastBuildUrl();

  protected abstract Unmarshaller getBuildUnmarshaller();

  protected void doLogin(HttpClient client) throws IOException {
    if (!StringUtils.isBlank(getUsername()) && !StringUtils.isBlank(getPassword())) {
      client.getParams().setAuthenticationPreemptive(true);
      Credentials defaultcreds = new UsernamePasswordCredentials(getUsername(), getPassword());
      client.getState().setCredentials(AuthScope.ANY, defaultcreds);
    }
  }
}
