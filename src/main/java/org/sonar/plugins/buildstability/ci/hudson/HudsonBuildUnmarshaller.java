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
