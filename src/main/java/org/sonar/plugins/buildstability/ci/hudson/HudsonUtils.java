/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2010 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */

package org.sonar.plugins.buildstability.ci.hudson;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;

import java.io.IOException;

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
    String hudsonLoginEntryUrl = hostName + "loginEntry";
    GetMethod loginLink = new GetMethod(hudsonLoginEntryUrl);
    client.executeMethod(loginLink);
    checkResult(loginLink.getStatusCode(), hudsonLoginEntryUrl);

    String location = hostName + "j_security_check";
    while (true) {
      PostMethod loginMethod = new PostMethod(location);
      loginMethod.addParameter("j_username", username);
      loginMethod.addParameter("j_password", password);
      loginMethod.addParameter("action", "login");
      client.executeMethod(loginMethod);
      if (loginMethod.getStatusCode() / 100 == 3) {
        // Commons HTTP client refuses to handle redirects for POST
        // so we have to do it manually.
        location = loginMethod.getResponseHeader("Location").getValue();
        continue;
      }
      checkResult(loginMethod.getStatusCode(), location);
      break;
    }
  }

  private static void checkResult(int httpStatusCode, String hudsonLoginEntryUrl) throws IOException {
    if (httpStatusCode != 200) {
      throw new IOException("Unable to access the Hudson page : " + hudsonLoginEntryUrl + ". HTTP status code : " + httpStatusCode);
    }
  }
}
