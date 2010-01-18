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
import org.apache.maven.model.CiManagement;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Project;
import org.sonar.api.test.IsMeasure;
import org.sonar.plugins.buildstability.ci.Build;
import org.sonar.plugins.buildstability.ci.CiConnector;

import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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
  public void testShouldExecuteOnProject() throws Exception {
    Project project = mock(Project.class);
    MavenProject mavenProject = mock(MavenProject.class);
    when(mavenProject.getCiManagement()).thenReturn(null, new CiManagement());
    when(project.getPom()).thenReturn(mavenProject);

    assertFalse(sensor.shouldExecuteOnProject(project));
    assertTrue(sensor.shouldExecuteOnProject(project));
  }

  @Test
  public void testAnalyse() throws Exception {
    BuildStabilitySensor sensor = spy(this.sensor);
    Project project = mock(Project.class);
    MavenProject mavenProject = mock(MavenProject.class);
    when(mavenProject.getCiManagement()).thenReturn(new CiManagement());
    when(project.getPom()).thenReturn(mavenProject);
    when(project.getConfiguration()).thenReturn(new BaseConfiguration());
    CiConnector connector = mock(CiConnector.class);
    when(connector.getBuilds(25)).thenReturn(Arrays.asList(
        new Build(1, "Fake", true, 10),
        new Build(2, "Fake", false, 4)
    ));
    Mockito.doReturn(connector).when(sensor).getConnector((String) any(), (String) any(), (String) any(), (String) any());
    SensorContext context = mock(SensorContext.class);

    sensor.analyse(project, context);

    Mockito.verify(context).saveMeasure(argThat((new IsMeasure(BuildStabilityMetrics.SUCCESSFUL, 1.0))));
    Mockito.verify(context).saveMeasure(argThat((new IsMeasure(BuildStabilityMetrics.FAILED, 1.0))));
    Mockito.verify(context).saveMeasure(argThat((new IsMeasure(BuildStabilityMetrics.SUCCESS_RATE, 50.0))));
    Mockito.verify(context).saveMeasure(argThat((new IsMeasure(BuildStabilityMetrics.AVG_DURATION, 7.0))));
  }
}
