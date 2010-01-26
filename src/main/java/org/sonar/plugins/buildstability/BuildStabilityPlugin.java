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
        project = true,
        module = true,
        global = true
    ),
    @Property(
        key = BuildStabilitySensor.USERNAME_PROPERTY,
        defaultValue = "",
        name = "Username",
        description = "Username to connect with Continuous Integration Server. Leave blank for anonymous.",
        project = true,
        module = true,
        global = true
    ),
    @Property(
        key = BuildStabilitySensor.PASSWORD_PROPERTY,
        defaultValue = "",
        name = "Password",
        description = "Password to connect with Continuous Integration Server. Leave blank for anonymous.",
        project = true,
        module = true,
        global = true
    ),
    @Property(
        key = BuildStabilitySensor.USE_JSECURITYCHECK_PROPERTY,
        defaultValue = BuildStabilitySensor.USE_JSECURITYCHECK_DEFAULT_VALUE + "",
        name = "Use j_security_check",
        description = "Set this property to true, if your Hudson delegates security to servlet container",
        project = true,
        module = true,
        global = true
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
        BuildStabilityWidget.class
    );
  }
}
