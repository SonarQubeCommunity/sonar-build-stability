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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.sonar.plugins.buildstability.ci.api.AbstractServer;
import org.sonar.plugins.buildstability.ci.api.Build;
import org.sonar.plugins.buildstability.ci.api.Unmarshaller;

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

  public void setUseJSecurityCheck(boolean useJSecurityCheck) {
    this.useJSecurityCheck = useJSecurityCheck;
  }

  public void doLogin(HttpClient client) throws IOException {
    if (!useJSecurityCheck) {
      super.doLogin(client);
    } else if (isAuthenticatedLogin()) {
      doLogin(client,getHost(), getUsername(), getPassword());
    }
  }

  protected void doLogin(HttpClient client, String hostName, String username, String password) throws IOException {
    String hudsonLoginEntryUrl = hostName + "/login";
    HttpGet loginLink = new HttpGet(hudsonLoginEntryUrl);
    HttpResponse response = client.execute(loginLink);
    checkResult(response.getStatusLine().getStatusCode(), hudsonLoginEntryUrl);
    EntityUtils.consume(response.getEntity());

    String location = hostName + "/j_acegi_security_check";
    boolean loggedIn = false;
    while (!loggedIn) {
      HttpPost loginMethod = new HttpPost(location);
      List<NameValuePair> nvps = new ArrayList<NameValuePair>();
      nvps.add(new BasicNameValuePair("j_username", username));
      nvps.add(new BasicNameValuePair("j_password", password));
      nvps.add(new BasicNameValuePair("action", "login"));
      loginMethod.setEntity(new UrlEncodedFormEntity(nvps));
      try {
        HttpResponse response2 = client.execute(loginMethod);
        if (response2.getStatusLine().getStatusCode() / 100 == 3) {
          // Commons HTTP client refuses to handle redirects for POST
          // so we have to do it manually.
          location = response2.getFirstHeader("Location").getValue();
        } else {
          checkResult(response2.getStatusLine().getStatusCode(), location);
          loggedIn = true;
        }
        EntityUtils.consume(response2.getEntity());
      } finally {
        loginMethod.reset();
      }
    }
  }

  protected void checkResult(int httpStatusCode, String hudsonLoginEntryUrl) throws IOException {
    if (httpStatusCode != 200) {
      throw new IllegalStateException("Unable to access the Hudson page : " + hudsonLoginEntryUrl + ". HTTP status code: " + httpStatusCode);
    }
  }

}
