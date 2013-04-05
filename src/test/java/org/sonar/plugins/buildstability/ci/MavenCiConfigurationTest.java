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

import org.apache.maven.model.CiManagement;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Julien HENRY
 */
public class MavenCiConfigurationTest {

  private MavenCiConfiguration ciConfig;
  private MavenProject pom;

  @Before
  public void setUp() throws Exception {
    pom = mock(MavenProject.class);
    ciConfig = new MavenCiConfiguration(pom);
  }

  @Test
  public void shouldReturnNullWhenNoCiManagement() throws Exception {
    when(pom.getCiManagement()).thenReturn(null);

    assertThat(ciConfig.getSystem()).isNull();
    assertThat(ciConfig.getUrl()).isNull();
  }

  @Test
  public void shouldReturnCiManagement() throws Exception {
    CiManagement ci = mock(CiManagement.class);
    when(ci.getSystem()).thenReturn("Jenkins");
    when(ci.getUrl()).thenReturn("http://jenkins");
    when(pom.getCiManagement()).thenReturn(ci);

    assertThat(ciConfig.getSystem()).isEqualTo("Jenkins");
    assertThat(ciConfig.getUrl()).isEqualTo("http://jenkins");
  }
}
