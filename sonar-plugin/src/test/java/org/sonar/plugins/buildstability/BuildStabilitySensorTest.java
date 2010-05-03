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

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.maven.model.CiManagement;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Project;
import org.sonar.api.test.IsMeasure;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.*;

/**
 * @author Evgeny Mandrikov
 */
public class BuildStabilitySensorTest {
  private BuildStabilitySensor sensor;

  @Before
  public void setUp() throws Exception {
    sensor = new BuildStabilitySensor();
  }

  @Test
  public void urlInConfigurationTakesPrecedence() throws Exception {
    MavenProject mavenProject = new MavenProject();
    CiManagement ciManagement = new CiManagement();
    ciManagement.setSystem("Hudson");
    ciManagement.setUrl("pom");
    mavenProject.setCiManagement(ciManagement);
    Configuration configuration = new BaseConfiguration();
    configuration.setProperty(BuildStabilitySensor.CI_URL_PROPERTY, "Hudson:conf");
    Project project = mock(Project.class);
    when(project.getPom()).thenReturn(mavenProject);
    when(project.getConfiguration()).thenReturn(configuration);

    assertThat(sensor.getCiUrl(project), is("Hudson:conf"));
  }

  @Test
  public void testShouldExecuteOnProject() throws Exception {
    Project project = mock(Project.class);
    MavenProject mavenProject = mock(MavenProject.class);
    CiManagement ciManagement = new CiManagement();
    ciManagement.setSystem("Hudson");
    ciManagement.setUrl("http://localhost");
    Configuration configuration = mock(Configuration.class);
    when(configuration.getString(BuildStabilitySensor.CI_URL_PROPERTY)).thenReturn(null, "Hudson:http://localhost");
    when(project.isRoot()).thenReturn(true);
    when(project.getConfiguration()).thenReturn(configuration);
    when(mavenProject.getCiManagement()).thenReturn(null, null, ciManagement);
    when(project.getPom()).thenReturn(mavenProject);

    assertFalse(sensor.shouldExecuteOnProject(project));
    assertTrue(sensor.shouldExecuteOnProject(project));
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
