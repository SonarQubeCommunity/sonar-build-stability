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

  public Build toModel(Element rootElement) {
    Build build = new Build();

    Element result;
    Element results = rootElement.element("results");
    if (results != null) {
      result = results.element("result");
    }
    else {
      result = rootElement;
    }
    if (result == null) {
      return null;
    }

    String state = result.attributeValue("state");
    build.setNumber(Integer.parseInt(result.attributeValue("number")));
    build.setResult(state);

    SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_FORMAT);
    String buildStartedTime = result.elementText("buildStartedTime");
    // Remove ':' in the timezone because it is not was the Java format expect
    int timezoneColonSeparatorIndex = buildStartedTime.length() - 3;
    if (buildStartedTime.charAt(timezoneColonSeparatorIndex) == ':') {
      buildStartedTime = buildStartedTime.substring(0, timezoneColonSeparatorIndex) + buildStartedTime.substring(timezoneColonSeparatorIndex + 1);
    }
    try {
      Date date = sdf.parse(buildStartedTime);
      build.setTimestamp(date.getTime());
    } catch (ParseException ignored) {
    }
    build.setDuration(Double.parseDouble(result.elementText("buildDurationInSeconds")) * 1000);
    build.setSuccessful(SUCCESSFULL.equalsIgnoreCase(state));

    return build;
  }
}
