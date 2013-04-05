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

import org.junit.Test;
import org.sonar.plugins.buildstability.ci.bamboo.BambooServer;
import org.sonar.plugins.buildstability.ci.hudson.HudsonServer;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Julien HENRY
 */
public class CiFactoryTest {

  @Test
  public void testCreateBamboo() {
    CiConnector connector = CiFactory.create("Bamboo", "http://bamboo/browse/", "user", "pwd", false);

    assertThat(connector.getServer().getUsername()).isEqualTo("user");
    assertThat(connector.getServer().getPassword()).isEqualTo("pwd");
    assertThat(connector.getServer().getHost()).isEqualTo("http://bamboo");
    assertThat(connector.getServer()).isInstanceOf(BambooServer.class);
  }

  @Test
  public void testCreateHudson() {
    CiConnector connector = CiFactory.create("Hudson", "http://hudson/job/", "user", "pwd", false);

    assertThat(connector.getServer().getUsername()).isEqualTo("user");
    assertThat(connector.getServer().getPassword()).isEqualTo("pwd");
    assertThat(connector.getServer().getHost()).isEqualTo("http://hudson");
    assertThat(connector.getServer()).isInstanceOf(HudsonServer.class);
  }

  @Test
  public void testCreateJenkins() {
    CiConnector connector = CiFactory.create("Jenkins", "http://jenkins/job/", "user", "pwd", false);

    assertThat(connector.getServer().getUsername()).isEqualTo("user");
    assertThat(connector.getServer().getPassword()).isEqualTo("pwd");
    assertThat(connector.getServer().getHost()).isEqualTo("http://jenkins");
    assertThat(connector.getServer()).isInstanceOf(HudsonServer.class);
  }
}
