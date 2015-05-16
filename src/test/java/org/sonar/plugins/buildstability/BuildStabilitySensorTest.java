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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.TimeMachine;
import org.sonar.api.batch.TimeMachineQuery;
import org.sonar.api.config.PropertyDefinitions;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.Measure;
import org.sonar.api.resources.Project;
import org.sonar.api.test.IsMeasure;
import org.sonar.plugins.buildstability.ci.CiConnector;
import org.sonar.plugins.buildstability.ci.MavenCiConfiguration;
import org.sonar.plugins.buildstability.ci.api.Build;
import org.sonar.plugins.buildstability.ci.api.Status;

/**
 * Test class of {@link BuildStabilitySensor}
 * 
 * @author Evgeny Mandrikov
 */
public class BuildStabilitySensorTest {
  private BuildStabilitySensor sensor;
  private MavenCiConfiguration mavenCiConfig;
  private Settings settings = new Settings(new PropertyDefinitions(BuildStabilityPlugin.class));
  private BuildAsString buildMetric = new BuildAsString();
  private TimeMachine timeMachine;
  private CiConnector connector;

  @Before
  public void setUp() throws Exception {
    mavenCiConfig = mock(MavenCiConfiguration.class);
    timeMachine = mock(TimeMachine.class);
    connector = mock(CiConnector.class);
    sensor = new BuildStabilitySensor(settings, timeMachine, mavenCiConfig) {

      @Override
      protected CiConnector getConnector(final String ciUrl) {
        return connector;
      }
    };
    when(timeMachine.getMeasures(any(TimeMachineQuery.class))).thenReturn(new ArrayList<Measure>());
    
    // For coverage only
    Status.valueOf(Status.success.name());
  }

  @Test
  public void urlInConfigurationTakesPrecedence() throws Exception {
    when(mavenCiConfig.getSystem()).thenReturn("Hudson");
    when(mavenCiConfig.getUrl()).thenReturn("pom");
    settings.setProperty(BuildStabilitySensor.CI_URL_PROPERTY, "Hudson:conf");

    assertThat(sensor.getCiUrl(mock(Project.class)), is("Hudson:conf"));
    
 
  }

  @Test
  public void testGetConnector() throws Exception {
    Assert.assertNotNull(new BuildStabilitySensor(settings, timeMachine, mavenCiConfig)
        .getConnector("Jenkins:any/job/any"));
  }

  @Test
  public void testGetConnectorInvalidUrl() throws Exception {
    Assert.assertNull(new BuildStabilitySensor(settings, timeMachine, mavenCiConfig).getConnector("any:any"));
  }

  @Test
  public void testShouldExecuteOnProject() throws Exception {
    Project project = mock(Project.class);
    when(project.isRoot()).thenReturn(true);
    assertFalse(sensor.shouldExecuteOnProject(project));

    // SONARPLUGINS-1603
    BuildStabilitySensor sensorNoMaven = new BuildStabilitySensor(settings, timeMachine);
    assertFalse(sensorNoMaven.shouldExecuteOnProject(project));

    settings.setProperty(BuildStabilitySensor.CI_URL_PROPERTY, "Hudson:http://localhost");
    assertTrue(sensor.shouldExecuteOnProject(project));

    settings.removeProperty(BuildStabilitySensor.CI_URL_PROPERTY);
    when(mavenCiConfig.getSystem()).thenReturn("Hudson");
    assertFalse(sensor.shouldExecuteOnProject(project));

    settings.setProperty(BuildStabilitySensor.CI_URL_PROPERTY, "Hudson:http://localhost");
    when(mavenCiConfig.getSystem()).thenReturn("Hudson");
    when(mavenCiConfig.getUrl()).thenReturn("http://localhost");
    assertTrue(sensor.shouldExecuteOnProject(project));

    settings.removeProperty(BuildStabilitySensor.CI_URL_PROPERTY);
    assertTrue(sensor.shouldExecuteOnProject(project));

    project = mock(Project.class);
    when(project.isRoot()).thenReturn(false);
    assertFalse(sensor.shouldExecuteOnProject(project));
  }

  @Test
  public void testAnalyzeBuilds() throws Exception {
    SensorContext context = mock(SensorContext.class);
    List<Build> builds = Arrays.asList(new Build(1, 0, Status.success, 10), new Build(2, 1, Status.failed, 4),
        new Build(3, 10, Status.success, 3), new Build(4, 20, Status.unstable, 5));

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

    verify(context).saveMeasure(
        argThat(new IsMeasure(BuildStabilityMetrics.BUILDS_DETAILS, "1,0,2,10;2,1,0,4;3,10,2,3;4,20,1,5")));

    verifyNoMoreInteractions(context);
  }

  @Test
  public void testAnalyzeInvalidConnector() throws Exception {
    settings.setProperty(BuildStabilitySensor.CI_URL_PROPERTY, "Unknown:http://localhost");
    final SensorContext context = mock(SensorContext.class);
    sensor = new BuildStabilitySensor(settings, timeMachine, mavenCiConfig) {

      @Override
      protected CiConnector getConnector(final String ciUrl) {
        return null;
      }
    };
    sensor.analyse(null, context);

    // Check the context is untouched
    verify(context, Mockito.never()).saveMeasure(any(Measure.class));
  }

  @Test
  public void testAnalyzeInvalidSetting() throws Exception {
    settings.setProperty(BuildStabilitySensor.CI_URL_PROPERTY, "Unknown:http://localhost");
    settings.setProperty(BuildStabilitySensor.DAYS_PROPERTY, "-");
    final SensorContext context = mock(SensorContext.class);
    sensor.analyse(null, context);

    // Check the context is untouched
    verify(context, Mockito.never()).saveMeasure(any(Measure.class));
    
    Assert.assertNotNull(sensor.toString());
  }

  @Test
  public void testAnalyzeWithHistory() throws Exception {
    final long time = System.currentTimeMillis();

    // Add history
    final List<Measure> previousMeasures = new ArrayList<Measure>();
    final Measure<String> previousMeasure = new Measure<String>(BuildStabilityMetrics.BUILDS_DETAILS);
    previousMeasures.add(previousMeasure);
    final List<Build> previousBuilds = new ArrayList<Build>();
    previousBuilds.add(new Build(0, 0, Status.failed, 5));
    previousBuilds.add(new Build(1, time - 20, Status.success, 10));
    previousMeasure.setData(buildMetric.toString(previousBuilds));
    when(timeMachine.getMeasures(any(TimeMachineQuery.class))).thenReturn(previousMeasures);

    final SensorContext context = mock(SensorContext.class);
    final List<Build> builds = Arrays.asList(new Build(2, time - 19, Status.failed, 4), new Build(3, time - 10,
        Status.success, 3), new Build(4, time, Status.unstable, 5));
    when(connector.getBuildsSince(any(Date.class))).thenReturn(builds);
    settings.setProperty(BuildStabilitySensor.CI_URL_PROPERTY, "Hudson:http://localhost");

    sensor.analyse(null, context);

    verify(context).saveMeasure(argThat((new IsMeasure(BuildStabilityMetrics.BUILDS, 4.0))));
    verify(context).saveMeasure(argThat((new IsMeasure(BuildStabilityMetrics.FAILED, 1.0))));
    verify(context).saveMeasure(argThat((new IsMeasure(BuildStabilityMetrics.SUCCESS_RATE, 75.0))));

    verify(context).saveMeasure(argThat((new IsMeasure(BuildStabilityMetrics.AVG_DURATION, 6.0))));
    verify(context).saveMeasure(argThat(new IsMeasure(BuildStabilityMetrics.SHORTEST_DURATION, 3.0)));
    verify(context).saveMeasure(argThat(new IsMeasure(BuildStabilityMetrics.LONGEST_DURATION, 10.0)));

    verify(context).saveMeasure(argThat(new IsMeasure(BuildStabilityMetrics.AVG_TIME_TO_FIX, 9.0)));
    verify(context).saveMeasure(argThat(new IsMeasure(BuildStabilityMetrics.LONGEST_TIME_TO_FIX, 9.0)));
    verify(context).saveMeasure(argThat(new IsMeasure(BuildStabilityMetrics.AVG_BUILDS_TO_FIX, 1.0)));

    verify(context).saveMeasure(
        argThat(new IsMeasure(BuildStabilityMetrics.BUILDS_DETAILS, "1," + (time - 20) + ",2,10;2," + (time - 19)
            + ",0,4;3," + (time - 10) + ",2,3;4," + time + ",1,5")));

    verifyNoMoreInteractions(context);
  }


  @Test
  public void testCompleteAndPurgeBuildsNoHistory() throws Exception {
    final long time = System.currentTimeMillis();

    final List<Measure> previousMeasures = new ArrayList<Measure>();
    when(timeMachine.getMeasures(any(TimeMachineQuery.class))).thenReturn(previousMeasures);

    final List<Build> builds = Arrays.asList(new Build(2, time - 19, Status.failed, 4), new Build(3, time - 10,
        Status.success, 3), new Build(4, time, Status.unstable, 5));
    when(connector.getBuildsSince(any(Date.class))).thenReturn(builds);
    sensor.completeAndPurgeBuilds(builds, new Date(), null);
  }

  @Test
  public void testNoSuccessfulBuilds() throws Exception {
    SensorContext context = mock(SensorContext.class);
    List<Build> builds = Arrays.asList(new Build(1, 0, Status.failed, 10),new Build(2, 0, Status.failed, 10));

    sensor.analyseBuilds(builds, context);

    verify(context).saveMeasure(argThat(new IsMeasure(BuildStabilityMetrics.BUILDS, 2.0)));
    verify(context).saveMeasure(argThat(new IsMeasure(BuildStabilityMetrics.FAILED, 2.0)));
    verify(context).saveMeasure(argThat(new IsMeasure(BuildStabilityMetrics.SUCCESS_RATE, 0.0)));

    verify(context).saveMeasure(argThat(new IsMeasure(BuildStabilityMetrics.AVG_DURATION, 0.0)));
    verify(context).saveMeasure(argThat(new IsMeasure(BuildStabilityMetrics.SHORTEST_DURATION, 0.0)));
    verify(context).saveMeasure(argThat(new IsMeasure(BuildStabilityMetrics.LONGEST_DURATION, 0.0)));

    verify(context).saveMeasure(argThat(new IsMeasure(BuildStabilityMetrics.AVG_TIME_TO_FIX, 0.0)));
    verify(context).saveMeasure(argThat(new IsMeasure(BuildStabilityMetrics.LONGEST_TIME_TO_FIX, 0.0)));
    verify(context).saveMeasure(argThat(new IsMeasure(BuildStabilityMetrics.AVG_BUILDS_TO_FIX, 0.0)));

    verify(context).saveMeasure(argThat(new IsMeasure(BuildStabilityMetrics.BUILDS_DETAILS, "1,0,0,10;2,0,0,10")));

    verifyNoMoreInteractions(context);
  }

  @Test
  public void testNoFailedBuilds() throws Exception {
    SensorContext context = mock(SensorContext.class);
    List<Build> builds = Arrays.asList(new Build(1, 0, Status.success, 10));

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

    verify(context).saveMeasure(argThat(new IsMeasure(BuildStabilityMetrics.BUILDS_DETAILS, "1,0,2,10")));

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