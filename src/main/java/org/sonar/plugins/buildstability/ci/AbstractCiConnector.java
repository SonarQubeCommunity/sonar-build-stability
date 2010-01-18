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

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

import java.io.IOException;
import java.util.List;

/**
 * @author Evgeny Mandrikov
 */
public abstract class AbstractCiConnector implements CiConnector {
  private HttpClient client;

  protected AbstractCiConnector(String username, String password) {
    client = new HttpClient();
    if (!StringUtils.isBlank(username) && !StringUtils.isBlank(password)) {
      client.getParams().setAuthenticationPreemptive(true);
      Credentials defaultcreds = new UsernamePasswordCredentials(username, password);
      client.getState().setCredentials(AuthScope.ANY, defaultcreds);
    }
  }

  public List<Build> getBuilds(int count) throws Exception {
    throw new RuntimeException("Not implemented");
  }

  protected Document executeMethod(GetMethod method) throws IOException, DocumentException {
    client.executeMethod(method);
    if (method.getStatusCode() != 200) {
      throw new IOException("Unexpected status code: " + method.getStatusCode());
    }
    return new SAXReader().read(method.getResponseBodyAsStream());
  }
}
