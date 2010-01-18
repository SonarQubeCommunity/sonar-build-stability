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

import org.apache.commons.httpclient.methods.GetMethod;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * See <a href="http://confluence.atlassian.com/display/BAMBOO/Bamboo+REST+APIs">Bamboo REST APIs</a>.
 *
 * @author Evgeny Mandrikov
 */
public class BambooConnector extends AbstractCiConnector {
  public static final String SYSTEM = "Bamboo";

  private static final String PATTERN = "/browse/";
  private static final String SUCCESSFULL = "Successful";
  private static final String FAILED = "Failed";

  private String base;
  private String key;

  public BambooConnector(String url, String username, String password) {
    super(username, password);

    int i = url.indexOf(PATTERN);
    base = url.substring(0, i);
    key = url.substring(i + PATTERN.length());
  }

  @Override
  public List<Build> getBuilds(int count) throws IOException, DocumentException {
    List<Build> builds = new ArrayList<Build>();
    Document dom = executeMethod(new GetMethod(getUri(base, key, count)));
    List<Element> buildsElem = dom.getRootElement().element("builds").elements("build");
    for (Element buildElem : buildsElem) {
      int buildNumber = Integer.parseInt(buildElem.attributeValue("number"));
      String buildResult = buildElem.attributeValue("state");
      double buildDuration = Double.parseDouble(buildElem.elementText("buildDurationInSeconds")) * 1000;
      builds.add(new Build(
          buildNumber,
          buildResult,
          SUCCESSFULL.equalsIgnoreCase(buildResult),
          buildDuration
      ));
    }
    return builds;
  }

  private static String getUri(String base, String key, Integer count) {
    StringBuilder sb = new StringBuilder(base);
    sb.append("/rest/api/latest/build/");
    sb.append(key);
    sb.append("?os_authType=basic");
    sb.append(count != null ? "&expand=builds%5B0:" + (count - 1) + "%5D.build" : "&expand=builds.build");
    return sb.toString();
  }
}
