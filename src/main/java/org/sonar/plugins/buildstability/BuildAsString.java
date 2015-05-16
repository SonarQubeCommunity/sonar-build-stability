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
package org.sonar.plugins.buildstability;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.sonar.plugins.buildstability.ci.api.Build;
import org.sonar.plugins.buildstability.ci.api.Status;

/**
 * Building or serializing {@link Build} as raw String.
 * 
 * @author Fabrice Daugan
 */
public class BuildAsString {

  public static final char BUILD_SEPARATOR = ';';
  public static final char PROPERTY_SEPARATOR = ',';

  /**
   * Return a map where {@link Build} ordered by build number from the input string. Using {@value #BUILD_SEPARATOR} as
   * build separator, and {@value #PROPERTY_SEPARATOR} as property separator.
   * 
   * @param buildsAsData
   *          String representation of a list of builds.
   * @return the list of {@link Build} objects from a string.
   */
  public Map<Integer, Build> toBuilds(final String buildsAsData) {
    final Map<Integer, Build> result = new TreeMap<Integer, Build>();
    for (final String buildAsString : StringUtils.split(buildsAsData, BUILD_SEPARATOR)) {
      final Build build = toBuild(buildAsString);
      result.put(build.getNumber(), build);
    }
    return result;
  }

  /**
   * Return {@link Build} from the input string. Using {@value #PROPERTY_SEPARATOR} as property separator.
   * 
   * @param buildAsString
   *          String representation of a a build.
   * @return the {@link Build} object from a string.
   */
  public Build toBuild(String buildAsString) {
    final Build build = new Build();
    final String[] properties = StringUtils.split(buildAsString, PROPERTY_SEPARATOR);
    build.setNumber(Integer.parseInt(properties[0]));
    build.setTimestamp(Long.parseLong(properties[1]));
    build.setStatus(Status.values()[Integer.parseInt(properties[2])]);
    build.setDuration(Long.parseLong(properties[3]));
    return build;
  }

  /**
   * Return the {@link String} representation of given builds. Corresponds to the opposite of {@link #toBuilds(String)}.
   * 
   * @param builds
   *          the builds to serialize as String.
   * @return the {@link String} representation of builds.
   */
  public String toString(final Collection<Build> builds) {
    final StringBuilder result = new StringBuilder();
    for (final Build build : builds) {
      append(build, result).append(BUILD_SEPARATOR);
    }

    // Remove the trailing separator
    result.setLength(Math.max(0, result.length() - 1));
    return result.toString();
  }

  /**
   * Append the given build's properties.
   */
  protected StringBuilder append(final Build build, final StringBuilder result) {
    result.append(build.getNumber()).append(PROPERTY_SEPARATOR);
    result.append(build.getTimestamp()).append(PROPERTY_SEPARATOR);
    result.append(build.getStatus().ordinal()).append(PROPERTY_SEPARATOR);
    return result.append(build.getDuration());
  }
}
