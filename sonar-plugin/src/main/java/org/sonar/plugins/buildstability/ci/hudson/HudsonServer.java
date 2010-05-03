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
import org.apache.commons.lang.StringUtils;
import org.sonar.plugins.buildstability.ci.AbstractServer;
import org.sonar.plugins.buildstability.ci.Unmarshaller;

import java.io.IOException;

/**
 * See <a href="http://wiki.hudson-ci.org/display/HUDSON/Remote+access+API">Hudson Remote access API</a>.
 *
 * @author Evgeny Mandrikov
 */
public class HudsonServer extends AbstractServer {
  public static final String SYSTEM = "Hudson";
  public static final String PATTERN = "/job/";

  private static final Unmarshaller BUILD_UNMARSHALLER = new HudsonBuildUnmarshaller();

  private boolean useJSecurityCheck;

  @Override
  protected String getBuildUrl(String number) {
    return getHost() + "/job/" + getKey() + "/" + number + "/api/xml/";
  }

  @Override
  protected String getLastBuildUrl() {
    return getBuildUrl("lastBuild");
  }

  @Override
  protected Unmarshaller getBuildUnmarshaller() {
    return BUILD_UNMARSHALLER;
  }

  @Override
  protected void doLogin(HttpClient client) throws IOException {
    if (!isUseJSecurityCheck()) {
      super.doLogin(client);
      return;
    }
    if (!StringUtils.isBlank(getUsername()) && !StringUtils.isBlank(getPassword())) {
      HudsonUtils.doLogin(client, getHost(), getUsername(), getPassword());
    }
  }

  public boolean isUseJSecurityCheck() {
    return useJSecurityCheck;
  }

  public void setUseJSecurityCheck(boolean useJSecurityCheck) {
    this.useJSecurityCheck = useJSecurityCheck;
  }
}
