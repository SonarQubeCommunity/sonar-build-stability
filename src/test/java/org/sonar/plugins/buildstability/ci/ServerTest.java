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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sonar.plugins.buildstability.ci.api.AbstractServer;

/**
 * Test class of {@link AbstractServer}
 * 
 * @author Fabrice Daugan
 */
public class ServerTest {

  private AbstractServer server;
  private DefaultHttpClient client;

  @Before
  public void setUp() throws Exception {
    server = mock(AbstractServer.class);
    client = mock(DefaultHttpClient.class);
    Mockito.doCallRealMethod().when(server).doLogin(client);
  }

  @Test
  public void testIsAuthenticatedLoginNoCredential() throws Exception {
    when(server.isAuthenticatedLogin()).thenCallRealMethod();
    Assert.assertFalse(server.isAuthenticatedLogin());
  }

  @Test
  public void testIsAuthenticatedLoginNoPassword() throws Exception {
    when(server.isAuthenticatedLogin()).thenCallRealMethod();
    when(server.getUsername()).thenReturn("user");
    Assert.assertFalse(server.isAuthenticatedLogin());
  }

  @Test
  public void testIsAuthenticatedLogin() throws Exception {
    when(server.isAuthenticatedLogin()).thenCallRealMethod();
    when(server.getUsername()).thenReturn("user");
    when(server.getPassword()).thenReturn("pwd");
    Assert.assertTrue(server.isAuthenticatedLogin());
  }

  @Test
  public void testDoLoginNoAuthentication() throws Exception {
    server.doLogin(client);
    verifyNoMoreInteractions(client);
  }

  @Test
  public void testDefaultBehavior() throws Exception {
    Mockito.when(server.getBuildsSinceUrl(any(Date.class))).thenCallRealMethod();
    Mockito.when(server.getBuildsUrl(any(int.class))).thenCallRealMethod();
    Assert.assertNull(server.getBuildsSinceUrl(new Date()));
    Assert.assertNull(server.getBuildsUrl(0));
  }

  @Test
  public void testDoLogin() throws Exception {
    when(server.isAuthenticatedLogin()).thenReturn(true);
    when(server.getUsername()).thenReturn("user");
    when(server.getPassword()).thenReturn("pwd");

    final CredentialsProvider credentialsProvider = mock(CredentialsProvider.class);
    when(client.getCredentialsProvider()).thenReturn(credentialsProvider);
    server.doLogin(client);
    verify(credentialsProvider).setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("user", "pwd"));
  }
}
