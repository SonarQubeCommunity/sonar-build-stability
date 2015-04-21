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
package org.sonar.plugins.buildstability.ci.jenkins;

import org.sonar.plugins.buildstability.ci.jenkins.JenkinsUtils;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.junit.Test;
import org.sonar.api.charts.AbstractChartTest;
import org.sonar.api.utils.SonarException;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Julien HENRY
 */
public class JenkinsUtilsTest extends AbstractChartTest {

  @Test
  public void testDoLogin() throws Exception {
    HttpClient client = mock(HttpClient.class);
    HttpResponse response1 = mock(HttpResponse.class, RETURNS_DEEP_STUBS);
    when(client.execute(any(HttpUriRequest.class))).thenReturn(response1);
    when(response1.getStatusLine().getStatusCode()).thenReturn(200);
    JenkinsUtils.doLogin(client, "http://jenkins.sonarsource.com/", "admin", "pwd");
  }

  @Test
  public void testDoLoginWithRedirect() throws Exception {
    HttpClient client = mock(HttpClient.class);
    HttpResponse response1 = mock(HttpResponse.class, RETURNS_DEEP_STUBS);
    HttpResponse response2 = mock(HttpResponse.class, RETURNS_DEEP_STUBS);
    when(response2.getFirstHeader("Location").getValue()).thenReturn("http://anotherlocation");
    when(client.execute(any(HttpUriRequest.class))).thenReturn(response1, response2, response1);
    when(response1.getStatusLine().getStatusCode()).thenReturn(200);
    when(response2.getStatusLine().getStatusCode()).thenReturn(302);
    JenkinsUtils.doLogin(client, "http://jenkins.sonarsource.com/", "admin", "pwd");
  }

  @Test(expected = SonarException.class)
  public void testDoLoginFailed() throws Exception {
    HttpClient client = mock(HttpClient.class);
    HttpResponse response1 = mock(HttpResponse.class, RETURNS_DEEP_STUBS);
    when(client.execute(any(HttpUriRequest.class))).thenReturn(response1);
    when(response1.getStatusLine().getStatusCode()).thenReturn(404);
    JenkinsUtils.doLogin(client, "http://jenkins.sonarsource.com/", "admin", "pwd");
  }
}
