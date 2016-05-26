Build Stability Plugin
======================
[![Build Status](https://api.travis-ci.org/SonarQubeCommunity/sonar-build-stability.svg)](https://travis-ci.org/SonarQubeCommunity/sonar-build-stability)

## Description / Features
Generates reports based on information about builds from Continuous Integration System.

## Requirements
<table>
<tr><td>Plugin</td><td>1.1.2</td><td>1.2</td><td>1.3</td></tr>
<tr><td>SonarQube</td><td>2.2+</td><td>3.0+</td><td>4.5.2+</td></tr>
<tr><td>Bamboo</td><td>:white_check_mark:</td><td>:white_check_mark:</td><td>:white_check_mark:</td></tr>
<tr><td>Jenkins</td><td>:red_circle:</td><td>:white_check_mark:</td><td>:white_check_mark:</td></tr>
<tr><td>Hudson</td><td>:white_check_mark:</td><td>:white_check_mark:</td><td>:red_circle:</td></tr>
<tr><td>TeamCity</td><td>:red_circle:</td><td>:red_circle:</td><td>:white_check_mark:</td></tr>
</table>

## Usage
Specify your Continuous Integration Server Job (URL, credentials, etc.):
* Either through the web interface: at project level, go to Configuration > Settings > Build Stability
* Or in your pom.xml file for Maven projects:

Example for Bamboo:
```
<ciManagement>
  <system>Bamboo</system>
  <url>http://ci.codehaus.org/browse/SONAR-DEF</url>
</ciManagement>
```

Example for Jenkins:
```
<ciManagement>
  <system>Jenkins</system>
  <url>https://sonarplugins.ci.cloudbees.com/job/build-stability</url>
</ciManagement>
```
Example for TeamCity:
```
<ciManagement>
  <system>TeamCity</system>
  <url>http://teamcity:port/viewType.html?buildTypeId=SonarBuildStability_Install</url>
</ciManagement>
```

Non-Maven examples:
```
sonar.build-stability.url=Bamboo:${BAMBOO_URL}/browse/${PROJECT_KEY}
sonar.build-stability.url=Jenkins:${JENKINS_URL}/job/${JOB_NAME}
sonar.build-stability.url=TeamCity:${TEAMCITY_URL}/viewType.html?buildTypeId=${PROJECT_KEY}
```

Run a new quality analysis and the metrics will be fed.

## Known limitations
 * TeamCity build number format has to be configured so that build numbers are valid integers (%build.counter%)

