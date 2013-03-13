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
package org.sonar.plugins.buildstability.ci;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.sonar.api.utils.SonarException;

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

  private DefaultHttpClient client;
  private AbstractServer server;

  protected CiConnector(AbstractServer server) {
    this.server = server;
    client = new DefaultHttpClient();
    HttpConnectionParams.setSoTimeout(client.getParams(), TIMEOUT);
  }

  protected Build getLastBuild() throws IOException {
    Document dom = executeGet(server.getLastBuildUrl());
    if (dom == null) {
      return null;
    }
    return server.getBuildUnmarshaller().toModel(dom.getRootElement());
  }

  protected Build getBuild(String number) throws IOException {
    Document dom = executeGet(server.getBuildUrl(number));
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
    Build last = current;
    if (date.before(last.getDate())) {
      builds.add(current);
    }
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

  protected Document executeGet(String url) throws IOException {
    return execute(new HttpGet(url));
  }

  protected Document execute(HttpGet httpGet) throws IOException {
    HttpResponse httpResponse = client.execute(httpGet);
    try {
      int statusCode = httpResponse.getStatusLine().getStatusCode();
      if (statusCode == 404) {
        return null;
      }
      if (statusCode != 200) {
        throw new IOException("Unexpected status code: " + statusCode);
      }
      String response = EntityUtils.toString(httpResponse.getEntity());
      String encoding = discoverEncoding(httpResponse, response);
      SAXReader reader = new SAXReader();
      reader.setEncoding(encoding);
      return reader.read(IOUtils.toInputStream(response));
    } catch (DocumentException e) {
      throw new SonarException("Unable to parse response", e);
    } finally {
      httpGet.releaseConnection();
    }
  }

  private String discoverEncoding(HttpResponse httpResponse, String response) {
    Pattern pattern = Pattern.compile("<\\?xml(?: \\w*=\".*\") encoding=\"([^ ]*)\".*");
    Matcher matcher = pattern.matcher(response);
    String encoding = "UTF-8";
    if (matcher.matches()) {
      encoding = matcher.group(1);
    } else {
      String contentType = httpResponse.getLastHeader("Content-Type").getValue();
      pattern = Pattern.compile(".*charset=([^;]*).*");
      matcher = pattern.matcher(contentType);
      if (matcher.matches()) {
        encoding = matcher.group(1);
      }
    }
    return encoding;
  }
}
