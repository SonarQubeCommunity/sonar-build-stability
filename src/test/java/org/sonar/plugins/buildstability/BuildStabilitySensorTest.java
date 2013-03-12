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
import org.sonar.api.batch.SensorContext;
import org.sonar.api.config.PropertyDefinitions;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Project;
import org.sonar.api.test.IsMeasure;
import org.sonar.plugins.buildstability.ci.Build;
import org.sonar.plugins.buildstability.ci.MavenCiConfiguration;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * @author Evgeny Mandrikov
 */
public class BuildStabilitySensorTest {
  private BuildStabilitySensor sensor;
  private MavenCiConfiguration mavenCiConfig;
  private Settings settings = new Settings(new PropertyDefinitions(BuildStabilityPlugin.class));

  @Before
  public void setUp() throws Exception {
    mavenCiConfig = mock(MavenCiConfiguration.class);
    sensor = new BuildStabilitySensor(settings, mavenCiConfig);
  }

  @Test
  public void urlInConfigurationTakesPrecedence() throws Exception {
    when(mavenCiConfig.getSystem()).thenReturn("Hudson");
    when(mavenCiConfig.getUrl()).thenReturn("pom");
    settings.setProperty(BuildStabilitySensor.CI_URL_PROPERTY, "Hudson:conf");

    assertThat(sensor.getCiUrl(mock(Project.class)), is("Hudson:conf"));
  }

  @Test
  public void testShouldExecuteOnProject() throws Exception {
    Project project = mock(Project.class);
    when(project.isRoot()).thenReturn(true);
    assertFalse(sensor.shouldExecuteOnProject(project));

    // SONARPLUGINS-1603
    BuildStabilitySensor sensorNoMaven = new BuildStabilitySensor(settings);
    assertFalse(sensorNoMaven.shouldExecuteOnProject(project));

    settings.setProperty(BuildStabilitySensor.CI_URL_PROPERTY, "Hudson:http://localhost");
    assertTrue(sensor.shouldExecuteOnProject(project));

    when(mavenCiConfig.getSystem()).thenReturn("Hudson");
    when(mavenCiConfig.getUrl()).thenReturn("http://localhost");
    assertTrue(sensor.shouldExecuteOnProject(project));
  }

  @Test
  public void testAnalyzeBuilds() throws Exception {
    SensorContext context = mock(SensorContext.class);
    List<Build> builds = Arrays.asList(
        new Build(1, 0, "Fake", true, 10),
        new Build(2, 1, "Fake", false, 4),
        new Build(3, 10, "Fake", true, 3),
        new Build(4, 20, "Fake", true, 5)
        );

    sensor.analyseBuilds(builds, context);

    verify(context).saveMeasure(argThat((new IsMeasure(BuildStabilityMetrics.BUILDS, 4.0))));
    verify(context).saveMeasure(argThat((new IsMeasure(BuildStabilityMetrics.FAILED, 1.0))));
    verify(context).saveMeasure(argThat((new IsMeasure(BuildStabilityMetrics.SUCCESS_RATE, 75.0))));

    verify(context).saveMeasure(argThat((new IsMeasure(BuildStabilityMetrics.AVG_DURATION, 6.0))));
    verify(context).saveMeasure(argThat(new IsMeasure(BuildStabilityMetrics.SHORTEST_DURATION, 3.0)));
    verify(context).saveMeasure(argThat(new IsMeasure(BuildStabilityMetrics.LONGEST_DURATION, 10.0)));

    verify(context).saveMeasure(argThat(new IsMeasure(BuildStabilityMetrics.AVG_TIME_TO_FIX, 9.0)));
    verify(context).saveMeasure(argThat(new IsMeasure(BuildStabilityMetrics.LONGEST_TIME_TO_FIX, 9.0)));
    verify(context).saveMeasure(argThat(new IsMeasure(BuildStabilityMetrics.AVG_BUILDS_TO_FIX, 1.0)));

    verify(context).saveMeasure(argThat(new IsMeasure(BuildStabilityMetrics.DURATIONS, "1=0.01;2=0.0040;3=0.0030;4=0.0050")));
    verify(context).saveMeasure(argThat(new IsMeasure(BuildStabilityMetrics.RESULTS, "1=g;2=r;3=g;4=g")));

    verifyNoMoreInteractions(context);
  }

  @Test
  public void testNoSuccessfulBuilds() throws Exception {
    SensorContext context = mock(SensorContext.class);
    List<Build> builds = Arrays.asList(
        new Build(1, 0, "Fake", false, 10)
        );

    sensor.analyseBuilds(builds, context);

    verify(context).saveMeasure(argThat(new IsMeasure(BuildStabilityMetrics.BUILDS, 1.0)));
    verify(context).saveMeasure(argThat(new IsMeasure(BuildStabilityMetrics.FAILED, 1.0)));
    verify(context).saveMeasure(argThat(new IsMeasure(BuildStabilityMetrics.SUCCESS_RATE, 0.0)));

    verify(context).saveMeasure(argThat(new IsMeasure(BuildStabilityMetrics.AVG_DURATION, 0.0)));
    verify(context).saveMeasure(argThat(new IsMeasure(BuildStabilityMetrics.SHORTEST_DURATION, 0.0)));
    verify(context).saveMeasure(argThat(new IsMeasure(BuildStabilityMetrics.LONGEST_DURATION, 0.0)));

    verify(context).saveMeasure(argThat(new IsMeasure(BuildStabilityMetrics.AVG_TIME_TO_FIX, 0.0)));
    verify(context).saveMeasure(argThat(new IsMeasure(BuildStabilityMetrics.LONGEST_TIME_TO_FIX, 0.0)));
    verify(context).saveMeasure(argThat(new IsMeasure(BuildStabilityMetrics.AVG_BUILDS_TO_FIX, 0.0)));

    verify(context).saveMeasure(argThat(new IsMeasure(BuildStabilityMetrics.DURATIONS, "1=0.01")));
    verify(context).saveMeasure(argThat(new IsMeasure(BuildStabilityMetrics.RESULTS, "1=r")));

    verifyNoMoreInteractions(context);
  }

  @Test
  public void testNoFailedBuilds() throws Exception {
    SensorContext context = mock(SensorContext.class);
    List<Build> builds = Arrays.asList(
        new Build(1, 0, "Fake", true, 10)
        );

    sensor.analyseBuilds(builds, context);

    verify(context).saveMeasure(argThat(new IsMeasure(BuildStabilityMetrics.BUILDS, 1.0)));
    verify(context).saveMeasure(argThat(new IsMeasure(BuildStabilityMetrics.FAILED, 0.0)));
    verify(context).saveMeasure(argThat(new IsMeasure(BuildStabilityMetrics.SUCCESS_RATE, 100.0)));

    verify(context).saveMeasure(argThat(new IsMeasure(BuildStabilityMetrics.AVG_DURATION, 10.0)));
    verify(context).saveMeasure(argThat(new IsMeasure(BuildStabilityMetrics.SHORTEST_DURATION, 10.0)));
    verify(context).saveMeasure(argThat(new IsMeasure(BuildStabilityMetrics.LONGEST_DURATION, 10.0)));

    verify(context).saveMeasure(argThat(new IsMeasure(BuildStabilityMetrics.AVG_TIME_TO_FIX, 0.0)));
    verify(context).saveMeasure(argThat(new IsMeasure(BuildStabilityMetrics.LONGEST_TIME_TO_FIX, 0.0)));
    verify(context).saveMeasure(argThat(new IsMeasure(BuildStabilityMetrics.AVG_BUILDS_TO_FIX, 0.0)));

    verify(context).saveMeasure(argThat(new IsMeasure(BuildStabilityMetrics.DURATIONS, "1=0.01")));
    verify(context).saveMeasure(argThat(new IsMeasure(BuildStabilityMetrics.RESULTS, "1=g")));

    verifyNoMoreInteractions(context);
  }

  @Test
  public void testNoBuilds() throws Exception {
    SensorContext context = mock(SensorContext.class);
    List<Build> builds = Collections.emptyList();

    sensor.analyseBuilds(builds, context);

    verify(context).saveMeasure(argThat(new IsMeasure(BuildStabilityMetrics.BUILDS, 0.0)));
    verify(context).saveMeasure(argThat(new IsMeasure(BuildStabilityMetrics.FAILED, 0.0)));
    verify(context).saveMeasure(argThat(new IsMeasure(BuildStabilityMetrics.SUCCESS_RATE, 0.0)));

    verify(context).saveMeasure(argThat(new IsMeasure(BuildStabilityMetrics.AVG_DURATION, 0.0)));
    verify(context).saveMeasure(argThat(new IsMeasure(BuildStabilityMetrics.SHORTEST_DURATION, 0.0)));
    verify(context).saveMeasure(argThat(new IsMeasure(BuildStabilityMetrics.LONGEST_DURATION, 0.0)));

    verify(context).saveMeasure(argThat(new IsMeasure(BuildStabilityMetrics.AVG_TIME_TO_FIX, 0.0)));
    verify(context).saveMeasure(argThat(new IsMeasure(BuildStabilityMetrics.LONGEST_TIME_TO_FIX, 0.0)));
    verify(context).saveMeasure(argThat(new IsMeasure(BuildStabilityMetrics.AVG_BUILDS_TO_FIX, 0.0)));

    verifyNoMoreInteractions(context);
  }
}
