/*
 * Copyright (C) 2010 Evgeny Mandrikov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sonar.plugins.buildstability;

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.charts.AbstractChartTest;
import org.sonar.api.charts.ChartParameters;

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
    BufferedImage image = chart.generateImage(new ChartParameters(
        encode("v=", "11=5.0;12=10.0;13=10.0;14=20.0") +
//        encode("&c=", "11=f;12=t;13=t")
            "&w=350&h=200"
    ));
    assertChartSizeGreaterThan(image, 1000);
    saveChart(image, "BuildStabilityChartTest/simple.png");
//    displayTestPanel(image);
//    Thread.sleep(1000 * 30);
  }

  private String encode(String prefix, String val) throws UnsupportedEncodingException {
    return prefix + URLEncoder.encode(val, "UTF-8");
  }
}
