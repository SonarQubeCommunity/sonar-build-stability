/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2010 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */

package org.sonar.plugins.buildstability;

import org.sonar.api.Extension;
import org.sonar.api.Plugin;
import org.sonar.api.Properties;
import org.sonar.api.Property;

import java.util.Arrays;
import java.util.List;

/**
 * @author Evgeny Mandrikov
 */
@Properties({
    @Property(
        key = BuildStabilitySensor.DAYS_PROPERTY,
        defaultValue = BuildStabilitySensor.DAYS_DEFAULT_VALUE + "",
        name = "Days",
        description = "Number of days to analyze.",
        global = true,
        project = true,
        module = false
    ),
    @Property(
        key = BuildStabilitySensor.CI_URL_PROPERTY,
        defaultValue = "",
        name = "CiManagement",
        description = "Continuous Integration Server. Leave blank to take this value from <i>pom.xml</i>. Example:" +
            "\"Hudson:http://hudson.glassfish.org/job/hudson/\"" +
            "or \"Bamboo:http://ci.codehaus.org/browse/SONAR\".",
        global = false,
        project = true,
        module = false
    ),
    @Property(
        key = BuildStabilitySensor.USERNAME_PROPERTY,
        defaultValue = "",
        name = "Username",
        description = "Username to connect with Continuous Integration Server. Leave blank for anonymous.",
        global = true,
        project = true,
        module = false
    ),
    @Property(
        key = BuildStabilitySensor.PASSWORD_PROPERTY,
        defaultValue = "",
        name = "Password",
        description = "Password to connect with Continuous Integration Server. Leave blank for anonymous.",
        global = true,
        project = true,
        module = false
    ),
    @Property(
        key = BuildStabilitySensor.USE_JSECURITYCHECK_PROPERTY,
        defaultValue = BuildStabilitySensor.USE_JSECURITYCHECK_DEFAULT_VALUE + "",
        name = "Use j_security_check",
        description = "Set this property to true, if your Hudson delegates security to servlet container.",
        global = true,
        project = true,
        module = false
    )
})
public class BuildStabilityPlugin implements Plugin {
  public static final String KEY = "build-stability";

  public String getKey() {
    return KEY;
  }

  public String getName() {
    return "Build Stability";
  }

  public String getDescription() {
    return "Generates reports based on information about builds from Continuous Integration System " +
        "(supports <a href='http://hudson-ci.org/'>Hudson</a> and <a href='http://www.atlassian.com/software/bamboo'>Bamboo</a>).";
  }

  public List<Class<? extends Extension>> getExtensions() {
    return Arrays.asList(
        BuildStabilityMetrics.class,
        BuildStabilitySensor.class,
        BuildStabilityWidget.class,
        BuildStabilityChart.class
    );
  }
}
