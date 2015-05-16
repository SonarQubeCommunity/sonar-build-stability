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

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.sonar.api.charts.AbstractChart;
import org.sonar.api.charts.ChartParameters;
import org.sonar.plugins.buildstability.ci.api.Build;
import org.sonar.plugins.buildstability.ci.api.Status;

/**
 * @author Evgeny Mandrikov
 */
public class BuildStabilityChart extends AbstractChart {
  private static final int RESPONSIVE_THRESHOLD = 25;
  private static final String FONT_NAME = "SansSerif";
  private static final String PARAM_VALUES = "v";
  private static final String PARAM_FONT_SIZE = "fs";
  private final BuildAsString buildMetric = new BuildAsString();

  // Cache the mapping from Status to Color
  private static final EnumMap<Status, Color> STATUS_TO_COLOR = new EnumMap<Status, Color>(Status.class);
  static {
    STATUS_TO_COLOR.put(Status.success, new Color(0, 174, 0));
    STATUS_TO_COLOR.put(Status.unstable, new Color(255, 153, 0));
    STATUS_TO_COLOR.put(Status.failed, new Color(212, 51, 63));
  }

  @Override
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

  private CategoryPlot generateJFreeChart(ChartParameters params) {

    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    CategoryPlot plot = new CategoryPlot();
    Font font = getFont(params.getValue(PARAM_FONT_SIZE));

    final Collection<Build> builds = buildMetric.toBuilds(params.getValue(PARAM_VALUES, "", true)).values();
    configureDomainAxis(plot, builds);
    configureRangeAxis(plot, "s", font);
    configureRenderer(plot, builds);
    configureValues(dataset, builds);

    plot.setDataset(dataset);

    return plot;
  }

  private void configureValues(DefaultCategoryDataset dataset, final Collection<Build> builds) {
    if (builds.isEmpty()) {
      dataset.addValue((Number) 0.0, 0, "0");
    } else {
      for (final Build build : builds) {
        dataset.addValue((Number) (build.getDuration() / DateUtils.MILLIS_PER_SECOND), 0, build.getNumber());
      }
    }
  }

  private void configureRenderer(CategoryPlot plot, final Collection<Build> builds) {

    final List<Paint> paints = new ArrayList<Paint>();
    for (final Build build : builds) {
      paints.add(STATUS_TO_COLOR.get(build.getStatus()));
    }

    final BarRenderer renderer = new  BarRenderer() {

      @Override
      public Paint getItemPaint(final int row, final int column) {
        if (paints.isEmpty()) {
          return Color.GRAY;
        }
        return paints.get(column % paints.size());
      }
    };

    renderer.setDrawBarOutline(!isCompact(builds.size()));
    renderer.setSeriesItemLabelsVisible(0, true);
    renderer.setItemMargin(0.0f);
    plot.setRenderer(renderer);
  }

  private boolean isCompact(int nBuilds) {
    return nBuilds > RESPONSIVE_THRESHOLD;
  }

  private void configureDomainAxis(CategoryPlot plot, final Collection<Build> builds) {
    CategoryAxis categoryAxis = new CategoryAxis();
    categoryAxis.setTickLabelsVisible(false);
    categoryAxis.setUpperMargin(isCompact(builds.size()) ? 0 : 0.03);
    categoryAxis.setLowerMargin(isCompact(builds.size()) ? 0 : 0.03);
    categoryAxis.setCategoryMargin(isCompact(builds.size()) ? 0 : 0.2);
    plot.setDomainAxis(categoryAxis);
    plot.setDomainGridlinesVisible(false);
  }

  private Font getFont(String fontSize) {
    final int size = StringUtils.isBlank(fontSize) ? FONT_SIZE : Integer.parseInt(fontSize);
    return new Font(FONT_NAME, Font.PLAIN, size);
  }

  private void configureRangeAxis(CategoryPlot plot, String valueLabelSuffix, Font font) {
    NumberAxis numberAxis = new NumberAxis();
    numberAxis.setUpperMargin(0.1);
    numberAxis.setTickLabelFont(font);
    numberAxis.setTickLabelPaint(OUTLINE_COLOR);
    numberAxis.setNumberFormatOverride(new DecimalFormat("0'" + valueLabelSuffix + "'"));
    numberAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
    plot.setRangeAxis(numberAxis);
  }
}
