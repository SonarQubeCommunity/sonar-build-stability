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
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Evgeny Mandrikov
 */
public class CiConnector {

  private HttpClient client;
  private AbstractServer server;

  protected CiConnector(AbstractServer server) {
    this.server = server;
    client = new HttpClient();
  }

  protected Build getLastBuild() throws IOException {
    Document dom = executeGetMethod(server.getLastBuildUrl());
    return server.getBuildUnmarshaller().toModel(dom.getRootElement());
  }

  protected Build getBuild(String number) throws IOException {
    Document dom = executeGetMethod(server.getBuildUrl(number));
    return server.getBuildUnmarshaller().toModel(dom.getRootElement());
  }

  protected Build getBuild(int number) throws IOException {
    return getBuild(String.valueOf(number));
  }

  public List<Build> getBuilds(int count) throws IOException {
    server.doLogin(client);
    List<Build> builds = new ArrayList<Build>();
    Build last = getLastBuild();
    builds.add(last);
    for (int i = 1; i <= count; i++) {
      builds.add(getBuild(last.getNumber() - i));
    }
    return builds;
  }

  public List<Build> getBuildsSince(Date date) throws IOException {
    server.doLogin(client);
    List<Build> builds = new ArrayList<Build>();
    Build last = getLastBuild();
    int number = last.getNumber();
    while (date.before(last.getDate())) {
      builds.add(last);
      number--;
      last = getBuild(number);
    }
    return builds;
  }

  protected Document executeGetMethod(String url) throws IOException {
    return executeMethod(new GetMethod(url));
  }

  protected Document executeMethod(GetMethod method) throws IOException {
    client.executeMethod(method);
    if (method.getStatusCode() != 200) {
      throw new IOException("Unexpected status code: " + method.getStatusCode());
    }
    try {
      return new SAXReader().read(method.getResponseBodyAsStream());
    } catch (DocumentException e) {
      throw new IOException(e);
    }
  }

}
