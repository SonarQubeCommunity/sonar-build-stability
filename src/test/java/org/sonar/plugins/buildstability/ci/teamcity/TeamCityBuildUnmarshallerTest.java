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

import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.junit.Before;
import org.junit.Test;
import org.sonar.plugins.buildstability.ci.api.Build;
import org.sonar.plugins.buildstability.ci.api.Status;

import java.io.InputStream;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Test class of {@link TeamCityBuildUnmarshaller}
 * @author Alexei Guevara <alguevara@kijiji.ca>
 */
public class TeamCityBuildUnmarshallerTest {
  private TeamCityBuildUnmarshaller unmarshaller;

  @Before
  public void setUp() throws Exception {
    unmarshaller = new TeamCityBuildUnmarshaller();
  }

  @Test
  public void testUnmarshallResult() throws Exception {
    SAXReader reader = new SAXReader();
    reader.setEncoding("UTF-8");
    InputStream result = this.getClass().getResourceAsStream("result.xml");
    Document doc = reader.read(result);
    Build b = unmarshaller.toModel(doc.getRootElement());
    assertThat(b.getNumber()).isEqualTo(6);
    assertThat(b.getStatus()).isEqualTo(Status.success);
    assertThat(b.getTimestamp()).isGreaterThan(0);
    assertThat(b.getDuration()).isGreaterThan(0);
  }

  @Test
  public void testUnmarshallResultNoDate() throws Exception {
    SAXReader reader = new SAXReader();
    reader.setEncoding("UTF-8");
    InputStream result = this.getClass().getResourceAsStream("result-no-date.xml");
    Document doc = reader.read(result);
    Build b = unmarshaller.toModel(doc.getRootElement());
    assertThat(b.getNumber()).isEqualTo(6);
    assertThat(b.getStatus()).isEqualTo(Status.success);
    assertThat(b.getTimestamp()).isEqualTo(0);
    assertThat(b.getDuration()).isEqualTo(0);
  }

  @Test
  public void testUnmarshallResultNoEnd() throws Exception {
    SAXReader reader = new SAXReader();
    reader.setEncoding("UTF-8");
    InputStream result = this.getClass().getResourceAsStream("result-no-end.xml");
    Document doc = reader.read(result);
    Build b = unmarshaller.toModel(doc.getRootElement());
    assertThat(b.getNumber()).isEqualTo(6);
    assertThat(b.getStatus()).isEqualTo(Status.failed);
    assertThat(b.getTimestamp()).isGreaterThan(0);
    assertThat(b.getDuration()).isEqualTo(0);
  }
}
