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

package org.sonar.plugins.buildstability.ci.bamboo;

import org.dom4j.Element;
import org.sonar.plugins.buildstability.ci.Build;
import org.sonar.plugins.buildstability.ci.Unmarshaller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Evgeny Mandrikov
 */
public class BambooBuildUnmarshaller implements Unmarshaller<Build> {
  private static final String SUCCESSFULL = "Successful";
  private static final String FAILED = "Failed";

  /**
   * Bamboo date-time format. Example: 2010-01-04T11:02:17.114-0600
   */
  private static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

  public Build toModel(Element domElement) {
    Build build = new Build();

    String state = domElement.attributeValue("state");
    build.setNumber(Integer.parseInt(domElement.attributeValue("number")));
    build.setResult(state);

    SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_FORMAT);
    String buildStartedTime = domElement.elementText("buildStartedTime");
    try {
      Date date = sdf.parse(buildStartedTime);
      build.setTimestamp(date.getTime());
    } catch (ParseException ignored) {
    }
    build.setDuration(Double.parseDouble(domElement.elementText("buildDurationInSeconds")) * 1000);
    build.setSuccessful(SUCCESSFULL.equalsIgnoreCase(state));

    return build;
  }
}
