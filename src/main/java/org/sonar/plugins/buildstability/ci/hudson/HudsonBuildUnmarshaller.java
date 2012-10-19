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

package org.sonar.plugins.buildstability.ci.hudson;

import org.dom4j.Element;
import org.sonar.plugins.buildstability.ci.Build;
import org.sonar.plugins.buildstability.ci.Unmarshaller;

/**
 * @author Evgeny Mandrikov
 */
public class HudsonBuildUnmarshaller implements Unmarshaller<Build> {
  public Build toModel(Element domElement) {
    Build build = new Build();

    String result = domElement.elementText("result");
    build.setNumber(Integer.parseInt(domElement.elementText("number")));
    build.setTimestamp(Long.parseLong(domElement.elementText("timestamp")));
    build.setResult(result);
    build.setDuration(Long.parseLong(domElement.elementText("duration")));
    build.setSuccessful("SUCCESS".equalsIgnoreCase(result));

    return build;
  }
}
