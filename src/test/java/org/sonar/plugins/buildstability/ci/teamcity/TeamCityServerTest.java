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
package org.sonar.plugins.buildstability.ci.teamcity;

import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Alexei Guevara <alguevara@kijiji.ca>
 */
public class TeamCityServerTest {
  private TeamCityServer server;

  @Before
  public void setUp() throws Exception {
    server = new TeamCityServer();
    server.setHost("http://host:1111");
    server.setKey("PROJECT_ID");
  }

  @Test
  public void testLastBuildURL() throws Exception {
    assertThat(server.getLastBuildUrl()).isEqualTo("http://host:1111/httpAuth/app/rest/buildTypes/id:PROJECT_ID/builds/count:0");
  }

  @Test
  public void testGetBuildURLByNumber() throws Exception {
    assertThat(server.getBuildUrl("1")).isEqualTo("http://host:1111/httpAuth/app/rest/buildTypes/id:PROJECT_ID/builds/number:1");
  }

  @Test
  public void testApi() throws Exception {
    assertThat(server.getBuildUnmarshaller()).isNotNull();
  }
}
