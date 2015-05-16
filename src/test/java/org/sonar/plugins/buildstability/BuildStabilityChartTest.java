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
package org.sonar.plugins.buildstability;

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.charts.ChartParameters;
import org.sonar.plugins.buildstability.util.AbstractChartTest;

import java.awt.image.BufferedImage;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static org.junit.Assert.assertNotNull;

/**
 * @author Evgeny Mandrikov
 */
public class BuildStabilityChartTest extends AbstractChartTest {
  private BuildStabilityChart chart;

  @Before
  public void setUp() throws Exception {
    chart = new BuildStabilityChart();
  }

  @Test
  public void testGetKey() throws Exception {
    assertNotNull(chart.getKey());
  }

  @Test
  public void simple() throws Exception {
    generate(0, "simple.png");
  }

  @Test
  public void simpleResponsive() throws Exception {
    generate(100, "simple-responsive.png");
  }

  @Test
  public void simpleResponsiveFontSize() throws Exception {
    generate(100, "simple-fs.png","&fs=10");
  }

  @Test
  public void empty() throws Exception {
    BufferedImage image = chart.generateImage(new ChartParameters("w=350&h=200"));
    assertChartSizeGreaterThan(image, 1000);
    saveChart(image, "BuildStabilityChartTest/empty.png");
  }

  protected void generate(int more, final String fileName) throws Exception {
    generate(more, fileName, "");
  }

  protected void generate(int more, final String fileName, final String args) throws Exception {
    final StringBuilder builder = new StringBuilder();
    builder.append("1,0,2,10000;2,1,0,4000;3,10,2,3000;4,8,1,5000;");
    for (int i = 5; i < more; i++) {
      builder.append("" + i);
      builder.append(",0,2," + (i % 10 + 1) + "000;");
    }
    builder.append("500,0,2,10000");

    BufferedImage image = chart.generateImage(new ChartParameters("w=350&h=200" + encode("&v=", builder.toString()) + args));
    assertChartSizeGreaterThan(image, 1000);
    saveChart(image, "BuildStabilityChartTest/" + fileName);
    // displayTestPanel(image);
    // Thread.sleep(1000 * 30);
  }

  private String encode(String prefix, String val) throws UnsupportedEncodingException {
    return prefix + URLEncoder.encode(val, "UTF-8");
  }
}
