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
    BufferedImage image = chart.generateImage(new ChartParameters("w=350&h=200" +
      encode("&v=", "11=5.0;12=10.0;13=10.0;14=20.0") +
      encode("&c=", "11=r;12=o;13=g")
      ));
    assertChartSizeGreaterThan(image, 1000);
    saveChart(image, "BuildStabilityChartTest/simple.png");
    // displayTestPanel(image);
    // Thread.sleep(1000 * 30);
  }

  private String encode(String prefix, String val) throws UnsupportedEncodingException {
    return prefix + URLEncoder.encode(val, "UTF-8");
  }
}
