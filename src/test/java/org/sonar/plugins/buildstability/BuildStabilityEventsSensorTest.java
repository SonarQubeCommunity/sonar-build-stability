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
import org.sonar.api.batch.Event;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Project;
import org.sonar.plugins.buildstability.ci.Build;

import java.util.Arrays;
import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Evgeny Mandrikov
 */
public class BuildStabilityEventsSensorTest {
  private Project project;
  private SensorContext context;

  @Before
  public void setUp() {
    project = mock(Project.class);
    context = mock(SensorContext.class);
  }

  @Test
  public void doNotTouchOtherCategories() throws Exception {
    Event build = new Event("Build", "Description", BuildStabilityEventsSensor.CATEGORY_BUILD, null);
    build.setDate(new Date());
    when(context.getEvents(project)).thenReturn(Arrays.asList(
        new Event("0.1-SNAPSHOT", "Description", Event.CATEGORY_VERSION, null),
        new Event("Alert", "Description", Event.CATEGORY_ALERT, null),
        build
    ));

    assertThat(BuildStabilityEventsSensor.getBuildsFromEvents(project, context).size(), is(1));
  }

  @Test
  public void testParse() throws Exception {
    Date date = new Date();
    Event event = new Event("Build", "Description", BuildStabilityEventsSensor.CATEGORY_BUILD, date, 0);
    event.setData(""); // TODO

    Build build = BuildStabilityEventsSensor.parse(event);

    assertThat(build.getTimestamp(), is(date.getTime()));
  }
}
