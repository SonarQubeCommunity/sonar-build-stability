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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Evgeny Mandrikov
 */
public class CiConnector {
  private static final int TIMEOUT = 30 * 1000;

  private HttpClient client;
  private AbstractServer server;

  protected CiConnector(AbstractServer server) {
    this.server = server;
    client = new HttpClient();
    client.getParams().setSoTimeout(TIMEOUT);
  }

  protected Build getLastBuild() throws IOException {
    Document dom = executeGetMethod(server.getLastBuildUrl());
    if (dom == null) {
      return null;
    }
    return server.getBuildUnmarshaller().toModel(dom.getRootElement());
  }

  protected Build getBuild(String number) throws IOException {
    Document dom = executeGetMethod(server.getBuildUrl(number));
    if (dom == null) {
      return null;
    }
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
    Build current = getLastBuild();
    builds.add(current);
    Build last = current;
    int number = last.getNumber();
    while (date.before(last.getDate()) && number > 0) {
      number--;
      current = getBuild(number);
      if (current != null) {
        builds.add(current);
        last = current;
      }
    }
    return builds;
  }

  protected Document executeGetMethod(String url) throws IOException {
    return executeMethod(new GetMethod(url));
  }

  protected Document executeMethod(GetMethod method) throws IOException {
    client.executeMethod(method);
    if (method.getStatusCode() == 404) {
      return null;
    }
    if (method.getStatusCode() != 200) {
      throw new IOException("Unexpected status code: " + method.getStatusCode());
    }
    try {
      SAXReader reader = new SAXReader();
      String response = method.getResponseBodyAsString();
      Pattern pattern = Pattern.compile("<\\?xml(?: \\w*=\".*\") encoding=\"([^ ]*)\".*");
      Matcher matcher = pattern.matcher(response);
      String encoding = "UTF-8";
      if (matcher.matches()) {
        encoding = matcher.group(1);
      } else {
        String contentType = method.getResponseHeader("Content-Type").getValue();
        pattern = Pattern.compile(".*charset=([^;]*).*");
        matcher = pattern.matcher(contentType);
        if (matcher.matches()) {
          encoding = matcher.group(1);
        }
      }
      reader.setEncoding(encoding);
      return reader.read(method.getResponseBodyAsStream());
    } catch (DocumentException e) {
      throw new RuntimeException(e);
    }
  }
}
