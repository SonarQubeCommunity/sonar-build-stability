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
package org.sonar.plugins.buildstability.ci.bamboo;

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.charts.AbstractChartTest;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Julien HENRY
 */
public class BambooServerTest extends AbstractChartTest {
  private BambooServer server;

  @Before
  public void setUp() throws Exception {
    server = new BambooServer();
    server.setHost("http://bamboo");
    server.setKey("KEY");
  }

  @Test
  public void testLastBuildURL() throws Exception {
    assertThat(server.getLastBuildUrl()).isEqualTo("http://bamboo/rest/api/latest/result/KEY?os_authType=basic&expand=results%5B0%5D.result");
  }

  @Test
  public void testGetBuildURLByNumber() throws Exception {
    assertThat(server.getBuildUrl("1")).isEqualTo("http://bamboo/rest/api/latest/result/KEY/1?os_authType=basic&expand=results.result");
  }
}
