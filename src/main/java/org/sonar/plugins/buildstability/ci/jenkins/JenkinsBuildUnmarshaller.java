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
package org.sonar.plugins.buildstability.ci.jenkins;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;
import org.sonar.plugins.buildstability.ci.api.Build;
import org.sonar.plugins.buildstability.ci.api.Status;
import org.sonar.plugins.buildstability.ci.api.UnmarshallerBatch;

/**
 * @author Evgeny Mandrikov
 */
public class JenkinsBuildUnmarshaller implements UnmarshallerBatch<Build> {

  @Override
  public Build toModel(Element domElement) {
    Build build = new Build();

    String result = domElement.elementText("result");
    build.setNumber(Integer.parseInt(domElement.elementText("number")));
    build.setTimestamp(Long.parseLong(domElement.elementText("timestamp")));
    build.setDuration(Long.parseLong(domElement.elementText("duration")));
    build.setStatus("SUCCESS".equalsIgnoreCase(result)?Status.success:"UNSTABLE".equalsIgnoreCase(result)?Status.unstable:Status.failed);
    if (build.getDuration() == 0) {
      // Incomplete build -> estimate
      build.setDuration(System.currentTimeMillis() - build.getTimestamp());
    }
    return build;
  }

  @Override
  public List<Build> toModels(final Element domElement) {
    final List<Build> result = new ArrayList<Build>();
    for (final Object element : domElement.elements("build")) {
      result.add(toModel((Element) element));
    }
    return result;
  }
}
