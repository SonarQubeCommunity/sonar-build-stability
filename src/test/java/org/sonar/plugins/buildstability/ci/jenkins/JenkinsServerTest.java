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

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sonar.plugins.buildstability.ci.api.UnmarshallerBatch;

/**
 * Test class of {@link JenkinsServer}
 * 
 * @author Julien HENRY
 */
public class JenkinsServerTest {
  private static final String MOCK_SERVER = "http://jenkins/";
  private JenkinsServer server;

  @Before
  public void setUp() throws Exception {
    server = new JenkinsServer();
    server.setHost("http://jenkins");
    server.setKey("project");
  }

  @Test
  public void testLastBuildURL() throws Exception {
    assertThat(server.getLastBuildUrl()).isEqualTo("http://jenkins/job/project/lastBuild/api/xml/");
  }

  @Test
  public void testGetBuildURLByNumber() throws Exception {
    assertThat(server.getBuildUrl("1")).isEqualTo("http://jenkins/job/project/1/api/xml/");
  }

  @Test
  public void testGetBuildsURLByDate() throws Exception {
    Date now = new Date();
    assertThat(server.getBuildsSinceUrl(now)).isEqualTo(
        "http://jenkins/job/project/api/xml?tree=builds[number,result,timestamp,duration]&xpath=//build[timestamp%20%3E%3D%20" + now.getTime() + "]&wrapper=builds");
  }

  @Test
  public void testGetBuildsURLByCount() throws Exception {
    assertThat(server.getBuildsUrl(5)).isEqualTo(
        "http://jenkins/job/project/api/xml?tree=builds[number,result,timestamp,duration]&xpath=//build[position()%20%3C%3D%205]&wrapper=builds");
  }

  @Test
  public void testGetBuildUnmarshaller() throws Exception {
    Assert.assertTrue(server.getBuildUnmarshaller() instanceof UnmarshallerBatch);
  }

  @Test
  public void testDoLoginNoSecurityCheck() throws Exception {
    HttpClient client = mock(HttpClient.class);
    server.setUseJSecurityCheck(false);
    server.doLogin(client);
    server.setUseJSecurityCheck(true);
    server.doLogin(client);
    verifyNoMoreInteractions(client);
  }

  @Test
  public void testIsAuthenticatedLogin() throws Exception {
    HttpClient client = mock(HttpClient.class);
    HttpResponse response1 = mock(HttpResponse.class, RETURNS_DEEP_STUBS);
    when(response1.getStatusLine().getStatusCode()).thenReturn(200);
    when(client.execute(any(HttpUriRequest.class))).thenReturn(response1);
    server.setPassword("pwd");
    server.setUsername("admin");
    server.setUseJSecurityCheck(true);
    server.doLogin(client);
  }

  @Test
  public void testDoLogin() throws Exception {
    HttpClient client = mock(HttpClient.class);
    HttpResponse response1 = mock(HttpResponse.class, RETURNS_DEEP_STUBS);
    when(client.execute(any(HttpUriRequest.class))).thenReturn(response1);
    when(response1.getStatusLine().getStatusCode()).thenReturn(200);
    server.doLogin(client, MOCK_SERVER, "admin", "pwd");
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
    server.doLogin(client, MOCK_SERVER, "admin", "pwd");
  }

  @Test(expected = IllegalStateException.class)
  public void testDoLoginFailed() throws Exception {
    HttpClient client = mock(HttpClient.class);
    HttpResponse response1 = mock(HttpResponse.class, RETURNS_DEEP_STUBS);
    when(client.execute(any(HttpUriRequest.class))).thenReturn(response1);
    when(response1.getStatusLine().getStatusCode()).thenReturn(404);
    server.doLogin(client, MOCK_SERVER, "admin", "pwd");
  }

  @Test(expected = ClientProtocolException.class)
  public void testDoLoginFailedConnect() throws Exception {
    HttpClient client = mock(HttpClient.class);
    HttpResponse response1 = mock(HttpResponse.class, RETURNS_DEEP_STUBS);
    when(response1.getStatusLine().getStatusCode()).thenReturn(200);

    when(client.execute(any(HttpUriRequest.class))).thenReturn(response1).thenThrow(new ClientProtocolException());
    server.doLogin(client, MOCK_SERVER, "admin", "pwd");
  }
}
