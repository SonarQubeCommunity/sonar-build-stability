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

import org.sonar.api.batch.Event;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Project;
import org.sonar.plugins.buildstability.ci.Build;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Evgeny Mandrikov
 */
public class BuildStabilityEventsSensor implements Sensor {
  public static final String CATEGORY_BUILD = "Build";

  public boolean shouldExecuteOnProject(Project project) {
    return true;
  }

  public void analyse(Project project, SensorContext context) {
    List<Build> builds = getBuildsFromEvents(project, context);
    // TODO don't create another sensor
    new BuildStabilitySensor().analyseBuilds(builds, context);
  }

  public static List<Build> getBuildsFromEvents(Project project, SensorContext context) {
    List<Build> builds = new ArrayList<Build>();
    for (Event event : context.getEvents(project)) {
      if (isBuildCategory(event)) {
        Build build = parse(event);
        if (build != null) {
          builds.add(build);
        }
      }
    }
    return builds;
  }

  protected static Build parse(Event event) {
    Build build = new Build();
    // TODO parse event.getData();
    build.setNumber(0);
    build.setTimestamp(event.getDate().getTime());
    build.setSuccessful(true);
    build.setDuration(0);
    return build;
  }

  private static boolean isBuildCategory(Event event) {
    return CATEGORY_BUILD.equalsIgnoreCase(event.getCategory());
  }
}
