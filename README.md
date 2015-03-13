Build Stability Plugin
======================

Download and Version information: http://update.sonarsource.org/plugins/buildstability-confluence.html

## Description / Features
Generates reports based on information about builds from Continuous Integration System.
## Requirements
<table>
<tr><td>Plugin</td><td>1.1.2</td><td>1.2</td></tr>
<tr><td>SonarQube</td><td>2.2+</td><td>3.0+</td></tr>
<tr><td>Bamboo</td><td>+</td><td>+</td></tr>
<tr><td>Jenkins</td><td>-</td><td>+</td></tr>
<tr><td>Hudson</td><td>+</td><td>+</td></tr>
<tr><td>TeamCity</td><td>-</td><td>-</td></tr>
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
  <url>https://sonarplugins.ci.cloudbees.com/job/build-stability/</url>
</ciManagement>
```

Non-Maven example for Jenkins:
```
sonar.build-stability.url=${JENKINS_URL}/job/${JOB_NAME}
```

Run a new quality analysis and the metrics will be fed.

### Security note for SonarQube 3.4.0 to 3.6.3 included
For the *.secured properties to be read during the project analysis, it is necessary to set the sonar.login and sonar.password properties to the credentials of a user that is:
* System administrator
* And project administrator on the project that is being analyzed

Example:

    sonar-runner -Dsonar.login=admin -Dsonar.password=admin
