/*
 * Copyright (C) 2010 Evgeny Mandrikov
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
    GetMethod loginLink = new GetMethod(hostName + "loginEntry");
    client.executeMethod(loginLink);
    checkResult(loginLink.getStatusCode());

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
      checkResult(loginMethod.getStatusCode());
      break;
    }
  }

  private static void checkResult(int i) throws IOException {
    if (i / 100 != 2) {
      throw new IOException();
    }
  }
}
