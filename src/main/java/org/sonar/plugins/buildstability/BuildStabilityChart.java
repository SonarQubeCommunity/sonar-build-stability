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

import org.apache.commons.lang.StringUtils;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.sonar.api.charts.AbstractChart;
import org.sonar.api.charts.ChartParameters;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
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

  static class ColoredBarRenderer extends BarRenderer {
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

    configureDomainAxis(plot);
    configureRangeAxis(plot, "s", font);
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
      paints.add("r".equals(keyValue[1]) ? Color.RED : Color.GREEN);
    }

    renderer.setColors(paints.toArray(new Paint[paints.size()]));
    renderer.setDrawBarOutline(true);
    renderer.setSeriesItemLabelsVisible(0, true);
    renderer.setItemMargin(0);
    plot.setRenderer(renderer);
  }

  private void configureDomainAxis(CategoryPlot plot) {
    CategoryAxis categoryAxis = new CategoryAxis();
    categoryAxis.setTickLabelsVisible(false);
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
