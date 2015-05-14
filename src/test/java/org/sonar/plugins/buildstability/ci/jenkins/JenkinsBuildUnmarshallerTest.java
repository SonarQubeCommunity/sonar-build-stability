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

import java.io.InputStream;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.junit.Before;
import org.junit.Test;
import org.sonar.plugins.buildstability.ci.api.Build;

/**
 * Test class of {@link JenkinsBuildUnmarshaller}
 * 
 * @author Fabrice Daugan
 */
public class JenkinsBuildUnmarshallerTest {
  private JenkinsBuildUnmarshaller unmarshaller;

  @Before
  public void setUp() throws Exception {
    unmarshaller = new JenkinsBuildUnmarshaller();
  }

  @Test
  public void testUnmarshallResult() throws Exception {
    final SAXReader reader = new SAXReader();
    reader.setEncoding("UTF-8");
    final InputStream result = this.getClass().getResourceAsStream("result.xml");
    final Document doc = reader.read(result);
    final Build build = unmarshaller.toModel(doc.getRootElement());
    assertThat(build.getNumber()).isEqualTo(32);
    assertThat(build.isSuccessful()).isTrue();
    assertThat(build.isUnstable()).isFalse();
    assertThat(build.getTimestamp()).isGreaterThan(0);
    assertThat(build.getDuration()).isGreaterThan(0);
  }

  @Test
  public void testUnmarshallResults() throws Exception {
    final SAXReader reader = new SAXReader();
    reader.setEncoding("UTF-8");
    final InputStream result = this.getClass().getResourceAsStream("results.xml");
    final Document doc = reader.read(result);
    final List<Build> builds = unmarshaller.toModels(doc.getRootElement());
    assertThat(builds.size()).isEqualTo(3);
    final Build build = builds.get(0);
    assertThat(build.getNumber()).isEqualTo(230);
    assertThat(build.isSuccessful()).isTrue();
    assertThat(build.isUnstable()).isTrue();
    assertThat(build.getTimestamp()).isGreaterThan(0);
    assertThat(build.getDuration()).isGreaterThan(0);

    final Build build2 = builds.get(2);
    assertThat(build2.getNumber()).isEqualTo(33);
    assertThat(build2.isSuccessful()).isFalse();
    assertThat(build2.isUnstable()).isFalse();
    assertThat(build2.getTimestamp()).isGreaterThan(0);
    assertThat(build2.getDuration()).isGreaterThan(0);
}
}
