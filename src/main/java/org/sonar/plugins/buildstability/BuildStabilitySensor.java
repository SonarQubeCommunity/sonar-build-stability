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

import org.apache.maven.model.CiManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.Measure;
import org.sonar.api.resources.Project;
import org.sonar.plugins.buildstability.ci.*;

import java.util.List;

/**
 * @author Evgeny Mandrikov
 */
public class BuildStabilitySensor implements Sensor {
  public static final String BUILDS_PROPERTY = "sonar.build-stability.builds";
  public static final int BUILDS_DEFAULT_VALUE = 25;
  public static final String USERNAME_PROPERTY = "sonar.build-stability.username";
  public static final String PASSWORD_PROPERTY = "sonar.build-stability.password";
  public static final String USE_JSECURITYCHECK_PROPERTY = "sonar.build-stability.use_jsecuritycheck";
  public static final boolean USE_JSECURITYCHECK_DEFAULT_VALUE = false;

  public boolean shouldExecuteOnProject(Project project) {
    CiManagement ci = project.getPom().getCiManagement();
    return ci != null;
  }

  public void analyse(Project project, SensorContext context) {
    Logger logger = LoggerFactory.getLogger(getClass());
    CiManagement ciManagement = project.getPom().getCiManagement();
    String system = ciManagement.getSystem();
    String url = ciManagement.getUrl();
    String username = project.getConfiguration().getString(USERNAME_PROPERTY);
    String password = project.getConfiguration().getString(PASSWORD_PROPERTY);
    boolean useJSecurityCheck = project.getConfiguration().getBoolean(USE_JSECURITYCHECK_PROPERTY, USE_JSECURITYCHECK_DEFAULT_VALUE);
    CiConnector connector = getConnector(system, url, username, password, useJSecurityCheck);
    if (connector == null) {
      logger.warn("Unknown CiManagement system: {}", system);
      return;
    }
    int buildsToRetrieve = project.getConfiguration().getInt(BUILDS_PROPERTY, BUILDS_DEFAULT_VALUE);
    List<Build> builds;
    try {
      builds = connector.getBuilds(buildsToRetrieve);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      return;
    }
    analyseBuilds(builds, context);
  }

  protected void analyseBuilds(List<Build> builds, SensorContext context) {
    double successful = 0;
    double failed = 0;
    double duration = 0;
    for (Build build : builds) {
      if (build.isSuccessfull()) {
        successful++;
        duration += build.getDuration();
      } else {
        failed++;
      }
    }

    double count = successful + failed;
    double avgDuration = successful != 0 ? duration / successful : 0;
    double sucessRate = count != 0 ? successful / count * 100 : 0;
    context.saveMeasure(new Measure(BuildStabilityMetrics.SUCCESSFUL, successful));
    context.saveMeasure(new Measure(BuildStabilityMetrics.FAILED, failed));
    context.saveMeasure(new Measure(BuildStabilityMetrics.SUCCESS_RATE, sucessRate));
    context.saveMeasure(new Measure(BuildStabilityMetrics.AVG_DURATION, avgDuration));
  }

  protected CiConnector getConnector(String system, String url, String username, String password, boolean useJSecurityCheck) {
    if (BambooConnector.SYSTEM.equalsIgnoreCase(system)) {
      return new BambooConnector(url, username, password);
    } else if (HudsonConnector.SYSTEM.equals(system)) {
      return new HudsonConnector(url, username, password, useJSecurityCheck);
    } else if (TeamCityConnector.SYSTEM.equalsIgnoreCase(system)) {
      return new TeamCityConnector(url, username, password);
    } else {
      return null;
    }
  }
}
