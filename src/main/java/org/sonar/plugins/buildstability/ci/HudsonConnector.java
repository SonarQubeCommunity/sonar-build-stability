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

package org.sonar.plugins.buildstability.ci;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * See <a href="http://wiki.hudson-ci.org/display/HUDSON/Remote+access+API">Hudson Remote access API</a>.
 *
 * @author Evgeny Mandrikov
 */
public class HudsonConnector extends AbstractCiConnector {
  public static final String SYSTEM = "Hudson";
  private static final String PATTERN = "/job/";

  private String base;
  private String key;
  private boolean useJSecurityCheck;

  public HudsonConnector(String url, String username, String password, boolean useJSecurityCheck) {
    super(username, password);
    this.useJSecurityCheck = useJSecurityCheck;

    int i = url.indexOf(PATTERN);
    base = url.substring(0, i);
    key = url.substring(i + PATTERN.length());
  }

  @Override
  public List<Build> getBuilds(int count) throws IOException, DocumentException {
    if (useJSecurityCheck && !StringUtils.isBlank(getUsername()) && !StringUtils.isBlank(getPassword())) {
      doLogin(getClient(), base + "/", getUsername(), getPassword());
    }
    List<Build> builds = new ArrayList<Build>();
    Build last = getBuild("lastBuild");
    builds.add(last);
    for (int i = 1; i < count; i++) {
      builds.add(getBuild(String.valueOf(last.getNumber() - i)));
    }
    return builds;
  }

  private Build getBuild(String number) throws IOException, DocumentException {
    GetMethod method = new GetMethod(base + "/job/" + key + "/" + number + "/api/xml/");
    Document dom = executeMethod(method);
    Element root = dom.getRootElement();
    int buildNumber = Integer.parseInt(root.elementText("number"));
    String buildResult = root.elementText("result");
    double buildDuration = Long.parseLong(root.elementText("duration"));
    return new Build(
        buildNumber,
        buildResult,
        "SUCCESS".equalsIgnoreCase(buildResult),
        buildDuration
    );
  }

  public static void doLogin(HttpClient client, String hostName, String username, String password) throws IOException {
    GetMethod loginLink = new GetMethod(hostName + "loginEntry");
    client.executeMethod(loginLink);
    checkResult(loginLink.getStatusCode());

    String location = hostName + "j_security_check";
    while (true) {
      PostMethod loginMethod = new PostMethod(location);
      loginMethod.addParameter("j_username", username); // TODO: replace with real user name and password
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

  /*
  public static void main(String[] args) throws IOException, DocumentException {
    System.out.println(
        new HudsonConnector(
            "http://localhost:8080/job/Test",
            "godin",
            "12345",
            false
        ).getBuilds(1)
    );
  }
  */
}
