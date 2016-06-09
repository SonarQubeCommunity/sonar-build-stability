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

import org.dom4j.Element;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.sonar.plugins.buildstability.ci.api.AbstractServer;
import org.sonar.plugins.buildstability.ci.api.Build;
import org.sonar.plugins.buildstability.ci.api.Unmarshaller;
import org.sonar.plugins.buildstability.ci.api.UnmarshallerBatch;
import org.sonar.plugins.buildstability.util.MockHttpServerInterceptor;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test class of {@link CiConnector}
 * 
 * @author Julien HENRY
 */
public class CiConnectorTest {

  private CiConnector connector;
  private AbstractServer server;

  @Rule
  public MockHttpServerInterceptor httpServer = new MockHttpServerInterceptor();
  private Unmarshaller<Build> unmarshaller;
  private Build lastBuild;

  @Before
  public void setUp() throws Exception {
    server = mock(AbstractServer.class);
    connector = new CiConnector(server);
    when(connector.getServer().getLastBuildUrl()).thenReturn("http://localhost:" + httpServer.getPort());
    unmarshaller = mock(Unmarshaller.class);
    when(server.getBuildUnmarshaller()).thenReturn(unmarshaller);
    lastBuild = mock(Build.class);
  }

  @Test
  public void testGetEncodingFromHttpHeader() throws Exception {
    httpServer.addMockResponseData("<?xml version=\"1.0\" standalone=\"yes\"?><foo>éàç</foo>");
    when(unmarshaller.toModel(any(Element.class))).thenReturn(lastBuild);

    assertThat(connector.getBuilds(1)).hasSize(1);
  }

  @Test
  public void testInvalidEncoding() throws Exception {
    httpServer.addMockResponseData("<?xml version=\"1.0\" standalone=\"yes\"?><foo>éàç</foo>");
    httpServer.setEncoding("éàç");
    when(unmarshaller.toModel(any(Element.class))).thenReturn(lastBuild);
    assertThat(connector.getBuilds(1)).hasSize(1);
  }

  @Test
  public void testNoEncoding() throws Exception {
    httpServer.addMockResponseData("<?xml version=\"1.0\" standalone=\"yes\"?><foo>éàç</foo>");
    httpServer.setEncoding("");
    when(unmarshaller.toModel(any(Element.class))).thenReturn(lastBuild);
    assertThat(connector.getBuilds(1)).hasSize(1);
  }

  @Test
  public void testGetBuilds() throws Exception {
    when(server.getBuildUrl(anyString())).thenReturn("http://localhost:" + httpServer.getPort());
    httpServer.addMockResponseData("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><foo></foo>");
    httpServer.addMockResponseData("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><foo></foo>");
    httpServer.addMockResponseData("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><foo></foo>");
    httpServer.addMockResponseData("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><foo></foo>");
    httpServer.addMockResponseData("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><foo></foo>");
    when(unmarshaller.toModel(any(Element.class))).thenReturn(lastBuild);

    assertThat(connector.getBuilds(5)).hasSize(5);
  }

  @Test
  public void testGetBuildsCountBatch() throws Exception {
    unmarshaller = mock(UnmarshallerBatch.class);
    when(server.getBuildUnmarshaller()).thenReturn(unmarshaller);
    when(server.getBuildsUrl(any(int.class))).thenReturn("http://localhost:" + httpServer.getPort());
    httpServer.addMockResponseData("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><foo></foo>");
    httpServer.addMockResponseData("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><builds></builds>");
    when(((UnmarshallerBatch<Build>) unmarshaller).toModels(any(Element.class))).thenReturn(Arrays.asList(new Build[5]));
    assertThat(connector.getBuilds(5)).hasSize(5);
  }

  @Test
  public void testGetBuildsSinceBatch() throws Exception {
    unmarshaller = mock(UnmarshallerBatch.class);
    when(server.getBuildUnmarshaller()).thenReturn(unmarshaller);
    when(server.getBuildsSinceUrl(any(Date.class))).thenReturn("http://localhost:" + httpServer.getPort());
    httpServer.addMockResponseData("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><foo></foo>");
    httpServer.addMockResponseData("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><builds></builds>");
    when(((UnmarshallerBatch<Build>) unmarshaller).toModels(any(Element.class))).thenReturn(Arrays.asList(new Build[5]));
    assertThat(connector.getBuildsSince(new Date())).hasSize(5);
  }

  @Test
  public void test404OnSomeBuilds() throws Exception {
    when(server.getBuildUrl(anyString())).thenReturn("http://localhost:" + httpServer.getPort());
    httpServer.addMockResponseData("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><foo></foo>");
    httpServer.addMockResponseData("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><foo></foo>");
    httpServer.addMockResponseStatusAndData(404, "");
    httpServer.addMockResponseData("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><foo></foo>");
    httpServer.addMockResponseStatusAndData(404, "");
    when(unmarshaller.toModel(any(Element.class))).thenReturn(lastBuild);

    assertThat(connector.getBuilds(5)).hasSize(3);
  }

  @Test
  public void test404OnLastBuild() throws Exception {
    httpServer.addMockResponseStatusAndData(404, "");
    when(unmarshaller.toModel(any(Element.class))).thenReturn(lastBuild);

    assertThat(connector.getBuilds(5)).hasSize(0);
  }

  @Test
  public void test404OnBuilds() throws Exception {
    httpServer.addMockResponseStatusAndData(404, "");
    when(unmarshaller.toModel(any(Element.class))).thenReturn(lastBuild);
    assertThat(connector.getAllBuilds("http://localhost:" + httpServer.getPort())).isNull();
  }

  @Test(expected = IllegalStateException.class)
  public void testHttpError() throws Exception {
    httpServer.addMockResponseStatusAndData(500, "");
    when(unmarshaller.toModel(any(Element.class))).thenReturn(lastBuild);

    connector.getBuilds(5);
  }

  @Test(expected = IllegalStateException.class)
  public void testInvalidXmlResponse() throws Exception {
    httpServer.addMockResponseData("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><invalid></foo>");
    when(unmarshaller.toModel(any(Element.class))).thenReturn(lastBuild);

    connector.getBuilds(5);
  }

  @Test
  public void testGetBuildsSince() throws Exception {
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    when(server.getBuildUrl(anyString())).thenReturn("http://localhost:" + httpServer.getPort());

    httpServer.addMockResponseData("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><foo></foo>");
    httpServer.addMockResponseData("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><foo></foo>");
    httpServer.addMockResponseData("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><foo></foo>");
    httpServer.addMockResponseData("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><foo></foo>");
    Build lastBuild = new Build(10, sdf.parse("05/04/2013").getTime(),null,0);
    Build otherBuild = new Build(9, sdf.parse("04/04/2013").getTime(),null,0);
    Build olderBuild = new Build(8, sdf.parse("03/04/2013").getTime(),null,0);
 
    when(unmarshaller.toModel(any(Element.class))).thenReturn(lastBuild, otherBuild, otherBuild, olderBuild);

    assertThat(connector.getBuildsSince(sdf.parse("03/04/2013"))).hasSize(3);
  }

  @Test
  public void testGetBuildsSinceNoLast() throws Exception {
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    httpServer.addMockResponseData("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><foo></foo>");
    when(server.getBuildUrl(anyString())).thenReturn("http://localhost:" + httpServer.getPort());
    when(unmarshaller.toModel(any(Element.class))).thenReturn(null);
    assertThat(connector.getBuildsSince(sdf.parse("03/04/2013"))).isEmpty();
  }

  @Test
  public void testGetBuildsSinceWithHole() throws Exception {
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    when(server.getBuildUrl(anyString())).thenReturn("http://localhost:" + httpServer.getPort());

    httpServer.addMockResponseData("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><foo></foo>");
    httpServer.addMockResponseData("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><foo></foo>");
    httpServer.addMockResponseData("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><foo></foo>");
    httpServer.addMockResponseData("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><foo></foo>");
    Build lastBuild = new Build(10, sdf.parse("05/04/2013").getTime(),null,0);
    Build otherBuild = new Build(9, sdf.parse("04/04/2013").getTime(),null,0);
    Build olderBuild = new Build(8, sdf.parse("01/04/2013").getTime(),null,0);
    when(unmarshaller.toModel(any(Element.class))).thenReturn(lastBuild, null, otherBuild, olderBuild);
    assertThat(connector.getBuildsSince(sdf.parse("02/04/2013"))).hasSize(2);
  }

  @Test
  public void testGetBuildsSinceLimitedByNumber() throws Exception {
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    when(server.getBuildUrl(anyString())).thenReturn("http://localhost:" + httpServer.getPort());

    httpServer.addMockResponseData("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><foo></foo>");
    httpServer.addMockResponseData("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><foo></foo>");
    Build lastBuild = new Build(2, sdf.parse("05/04/2013").getTime(),null,0);
    Build otherBuild = new Build(9, sdf.parse("04/04/2013").getTime(),null,0);
 
    when(unmarshaller.toModel(any(Element.class))).thenReturn(lastBuild, otherBuild);

    assertThat(connector.getBuildsSince(sdf.parse("01/04/2013"))).hasSize(2);
  }
}
