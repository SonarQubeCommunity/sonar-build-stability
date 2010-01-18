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
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * See <a href="http://wiki.hudson-ci.org/display/HUDSON/Remote+access+API">Hudson Remote access API</a>.
 *
 * @author Evgeny Mandrikov
 */
public class HudsonConnector extends AbstractCiConnector {
  public static final String SYSTEM = "Hudson";

  private String url;

  public HudsonConnector(String url, String username, String password) {
    super(username, password);
    this.url = url;
  }

  @Override
  public List<Build> getBuilds(int count) throws Exception {
    List<Build> builds = new ArrayList<Build>();
    Build last = getBuild(url, "lastBuild");
    builds.add(last);
    for (int i = 1; i < count; i++) {
      builds.add(getBuild(url, String.valueOf(last.getNumber() - i)));
    }
    return builds;
  }

  private Build getBuild(String job, String number) throws Exception {
    GetMethod method = new GetMethod(job + "/" + number + "/api/xml/");
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
}
