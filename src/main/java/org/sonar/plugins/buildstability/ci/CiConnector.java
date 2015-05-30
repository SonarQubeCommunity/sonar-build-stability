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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.plugins.buildstability.ci.api.AbstractServer;
import org.sonar.plugins.buildstability.ci.api.Build;
import org.sonar.plugins.buildstability.ci.api.UnmarshallerBatch;

import com.google.common.annotations.VisibleForTesting;

/**
 * @author Evgeny Mandrikov
 */
public class CiConnector {

  private static final Logger LOG = LoggerFactory.getLogger(CiConnector.class);
  private static final int TIMEOUT = 30 * (int) DateUtils.MILLIS_PER_SECOND;

  private DefaultHttpClient client;
  private AbstractServer server;

  protected CiConnector(AbstractServer server) {
    this.server = server;
    client = new DefaultHttpClient();
    HttpConnectionParams.setSoTimeout(client.getParams(), TIMEOUT);
  }

  @VisibleForTesting
  AbstractServer getServer() {
    return server;
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
    final String batchAllUrl = server.getBuildsUrl(count);
    final List<Build> builds;
    if (batchAllUrl == null) {
      // Iterate over builds until the desired count. 404 errors may occur
      builds = new ArrayList<Build>();
      Build last = getLastBuild();
      if (last != null) {
        builds.add(last);
        for (int i = 1; i < count; i++) {
          Build previous = getBuild(last.getNumber() - i);
          if (previous != null) {
            builds.add(previous);
          }
        }
      }
    } else {
      builds = getAllBuilds(batchAllUrl);
    }
    return builds;
  }

  public List<Build> getBuildsSince(Date date) throws IOException {
    server.doLogin(client);
    final String batchAllUrl = server.getBuildsSinceUrl(date);
    final List<Build> builds;
    if (batchAllUrl == null) {
      // Iterate over all builds, maybe hundreds, and 404 errors may occur
      builds = new ArrayList<Build>();
      Build current = getLastBuild();
      int number = current == null ? 0 : current.getNumber();
      while (number > 0 && (current == null || date.before(current.getDate()))) {
        if (current != null) {
          builds.add(current);
        }
        number--;
        if (number > 0) {
          current = getBuild(number);
        }
      }
    } else {
      builds = getAllBuilds(batchAllUrl);
    }
    return builds;
  }

  /**
   * Return all builds from the given URL.
   */
  protected List<Build> getAllBuilds(final String batchAllUrl) throws IOException {
    Document dom = executeGet(batchAllUrl);
    if (dom == null) {
      return null;
    }
    return ((UnmarshallerBatch<Build>) server.getBuildUnmarshaller()).toModels(dom.getRootElement());
  }

  protected Document executeGet(String url) throws IOException {
    return execute(new HttpGet(url));
  }

  protected Document execute(HttpGet httpGet) throws IOException {
    HttpResponse httpResponse = client.execute(httpGet);
    try {
      int statusCode = httpResponse.getStatusLine().getStatusCode();
      if (statusCode == 404) {
        LOG.warn("Received 404 when trying to access {}", httpGet.getURI());
        return null;
      }
      if (statusCode != 200) {
        throw new IllegalStateException("Received " + statusCode + " when trying to access " + httpGet.getURI());
      }
      String response = EntityUtils.toString(httpResponse.getEntity());
      String encoding = discoverEncoding(httpResponse, response);
      SAXReader reader = new SAXReader();
      reader.setEncoding(encoding);
      return reader.read(IOUtils.toInputStream(response, encoding));
    } catch (DocumentException e) {
      throw new IllegalStateException("Unable to parse response", e);
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
      final ContentType contentType = ContentType.get(httpResponse.getEntity());
      if (contentType != null && contentType.getCharset() != null) {
        encoding = contentType.getCharset().name();
      }
    }
    return encoding;
  }
}
