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

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.sonar.api.utils.SonarException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Evgeny Mandrikov
 */
public final class HudsonUtils {
  /**
   * Hide utility-class constructor.
   */
  private HudsonUtils() {
  }

  public static void doLogin(HttpClient client, String hostName, String username, String password) throws IOException {
    String hudsonLoginEntryUrl = hostName + "/loginEntry";
    HttpGet loginLink = new HttpGet(hudsonLoginEntryUrl);
    HttpResponse response = client.execute(loginLink);
    checkResult(response.getStatusLine().getStatusCode(), hudsonLoginEntryUrl);

    String location = hostName + "/j_security_check";
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
        }
        else {
          checkResult(response2.getStatusLine().getStatusCode(), location);
          loggedIn = true;
        }
      } finally {
        loginMethod.releaseConnection();
      }
    }
  }

  private static void checkResult(int httpStatusCode, String hudsonLoginEntryUrl) throws IOException {
    if (httpStatusCode != 200) {
      throw new SonarException("Unable to access the Hudson page : " + hudsonLoginEntryUrl + ". HTTP status code: " + httpStatusCode);
    }
  }
}
