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

import org.apache.commons.lang.StringUtils;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.sonar.api.charts.AbstractChart;
import org.sonar.api.charts.ChartParameters;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * @author Evgeny Mandrikov
 */
public class BuildStabilityChart extends AbstractChart {
  private static final String FONT_NAME = "SansSerif";
  private static final String PARAM_VALUES = "v";
  private static final String PARAM_COLORS = "c";
  private static final String PARAM_FONT_SIZE = "fs";

  public String getKey() {
    return "buildsbar";
  }

  @Override
  protected Plot getPlot(ChartParameters params) {
    CategoryPlot plot = generateJFreeChart(params);
    plot.setOutlinePaint(OUTLINE_COLOR);
    plot.setDomainGridlinePaint(GRID_COLOR);
    plot.setRangeGridlinePaint(GRID_COLOR);
    return plot;
  }

  class ColoredBarRenderer extends BarRenderer {
    private Paint[] colors;

    public void setColors(Paint[] colors) {
      this.colors = colors;
    }

    public Paint getItemPaint(final int row, final int column) {
      if (colors.length == 0) {
        return Color.GRAY;
      }
      return colors[column % colors.length];
    }
  }

  private CategoryPlot generateJFreeChart(ChartParameters params) {
    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    CategoryPlot plot = new CategoryPlot();

    Font font = getFont(params.getValue(PARAM_FONT_SIZE));
    configureDomainAxis(plot, font);
    configureRangeAxis(plot, "ms", font);
    configureRenderer(plot, params.getValue(PARAM_COLORS, "", true));
    configureValues(dataset, params.getValue(PARAM_VALUES, "", true));

    plot.setDataset(dataset);

    return plot;
  }

  private void configureValues(DefaultCategoryDataset dataset, String values) {
    String[] pairs = StringUtils.split(values, ";");
    if (pairs.length == 0) {
      dataset.addValue((Number) 0.0, 0, "0");
    } else {
      for (String pair : pairs) {
        String[] keyValue = StringUtils.split(pair, "=");
        double val = Double.parseDouble(keyValue[1]);
        dataset.addValue((Number) val, 0, keyValue[0]);
      }
    }
  }

  private void configureRenderer(CategoryPlot plot, String colors) {
    ColoredBarRenderer renderer = new ColoredBarRenderer();

    String[] pairs = StringUtils.split(colors, ";");
    ArrayList<Paint> paints = new ArrayList<Paint>();
    for (String pair : pairs) {
      String[] keyValue = StringUtils.split(pair, "=");
      paints.add("t".equals(keyValue[1]) ? Color.GREEN : Color.RED);
    }

    renderer.setColors(paints.toArray(new Paint[paints.size()]));
    renderer.setDrawBarOutline(true);
    renderer.setSeriesItemLabelsVisible(0, true);
    renderer.setItemMargin(0);
    plot.setRenderer(renderer);
  }

  private void configureDomainAxis(CategoryPlot plot, Font font) {
    CategoryAxis categoryAxis = new CategoryAxis();
    categoryAxis.setCategoryMargin(0);
    categoryAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
    categoryAxis.setTickMarksVisible(true);
    categoryAxis.setTickLabelFont(font);
    categoryAxis.setTickLabelPaint(OUTLINE_COLOR);
    plot.setDomainAxis(categoryAxis);
    plot.setDomainGridlinesVisible(false);
  }

  private Font getFont(String fontSize) {
    int size = FONT_SIZE;
    if (!StringUtils.isBlank(fontSize)) {
      size = Integer.parseInt(fontSize);
    }
    return new Font(FONT_NAME, Font.PLAIN, size);
  }

  private void configureRangeAxis(CategoryPlot plot, String valueLabelSuffix, Font font) {
    NumberAxis numberAxis = new NumberAxis();
    numberAxis.setUpperMargin(0.3);
    numberAxis.setTickLabelFont(font);
    numberAxis.setTickLabelPaint(OUTLINE_COLOR);
    String suffix = "";
    if (valueLabelSuffix != null && !"".equals(valueLabelSuffix)) {
      suffix = new StringBuilder().append("'").append(valueLabelSuffix).append("'").toString();
    }
    numberAxis.setNumberFormatOverride(new DecimalFormat("0" + suffix));
    numberAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
    plot.setRangeAxis(numberAxis);
  }
}
