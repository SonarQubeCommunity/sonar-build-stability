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
package org.sonar.plugins.buildstability.util;

import org.junit.rules.ExternalResource;

public final class MockHttpServerInterceptor extends ExternalResource {

  private MockHttpServer server;

  @Override
  protected final void before() throws Throwable {
    server = new MockHttpServer();
    server.start();
  }

  @Override
  protected void after() {
    server.stop();
  }

  public void addMockResponseData(String data) {
    server.addMockResponseData(data);
  }

  public void addMockResponseStatusAndData(int status, String data) {
    server.addMockResponseStatusAndData(status, data);
  }

  public int getPort() {
    return server.getPort();
  }

  public void setEncoding(final String encoding) {
    server.setEncoding(encoding);
  }
}
