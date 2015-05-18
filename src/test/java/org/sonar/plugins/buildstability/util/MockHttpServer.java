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

import org.apache.commons.io.IOUtils;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.apache.commons.io.IOUtils.write;

public final class MockHttpServer {
  private static final String DEFAULT_ENCODING = "text/xml;charset=utf-8";
  private Server server;
  private String responseBody;
  private String requestBody;
  private String encoding = DEFAULT_ENCODING;
  private Queue<String> mockResponseData = new LinkedList<String>();
  private Queue<Integer> mockResponseStatus = new LinkedList<Integer>();

  public void start() throws Exception {
    // 0 is random available port
    server = new Server(0);
    server.setHandler(getMockHandler());
    server.start();
  }

  /**
   * Creates an {@link AbstractHandler handler} returning an arbitrary String as a response.
   *
   * @return never <code>null</code>.
   */
  public Handler getMockHandler() {
    Handler handler = new AbstractHandler() {

      public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException {
        Request baseRequest = request instanceof Request ? (Request) request : HttpConnection.getCurrentConnection().getRequest();
        setResponseBody(mockResponseData.poll());
        setRequestBody(IOUtils.toString(baseRequest.getInputStream()));
        response.setStatus(mockResponseStatus.poll());
        response.setContentType(encoding);
        write(getResponseBody(), response.getOutputStream());
        baseRequest.setHandled(true);
      }
    };
    return handler;
  }

  public void stop() {
    try {
      if (server != null) {
        server.stop();
      }
    } catch (Exception e) {
      throw new IllegalStateException("Fail to stop HTTP server", e);
    }
  }

  public void setResponseBody(String responseBody) {
    this.responseBody = responseBody;
  }

  public String getResponseBody() {
    return responseBody;
  }

  public void setRequestBody(String requestBody) {
    this.requestBody = requestBody;
  }

  public String getRequestBody() {
    return requestBody;
  }

  public void addMockResponseData(String mockResponseData) {
    addMockResponseStatusAndData(SC_OK, mockResponseData);
  }

  public void addMockResponseStatusAndData(int status, String mockResponseData) {
    this.mockResponseStatus.add(status);
    this.mockResponseData.add(mockResponseData);
  }

  public int getPort() {
    return server.getConnectors()[0].getLocalPort();
  }

  public void setEncoding(final String encoding) {
    this.encoding = encoding;
  }
}
