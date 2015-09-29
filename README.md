# TraceTronic ECU-TEST Plug-in
This plug-in integrates Jenkins with [ECU-TEST](https://www.tracetronic.com/products/ecu-test/) and generates reports on automated test execution.

## Features
- Provides an easy integration and control of ECU-TEST and the Tool-Server with Jenkins
- Enables the execution of ECU-TEST packages and projects with their respective configurations

Moreover publishing test results is included in shape of:
 - Linking the TRF reports
 - Generating test report trends
 - Uploading ATX test reports to TEST-GUIDE
 - Parsing the ECU-TEST logs for warnings and errors

## System requirements
- Jenkins LTS 1.565.3 or higher
- Java SE Runtime Environment 1.7 or higher
- [ECU-TEST](https://www.tracetronic.com/products/ecu-test/) 6.3.0 or higher
- optional: [TEST-GUIDE](https://www.tracetronic.com/products/test-guide/) 1.11.0 or higher

## Building
To build the plug-in from source:

```
$> mvn clean package hpi:hpi
```

To run a local Jenkins instance with the plug-in installed:

```
$> mvn clean package hpi:run
```

## Installation
Follow the instructions at https://wiki.jenkins-ci.org/display/JENKINS/Plugins#Plugins-Howtoinstallplugins

## Configuration

### ECU-TEST configuration
- ECU-TEST installations are administrated in Jenkins system configuration at section "ECU-TEST"
- An installation entry is specified by an arbitrary name and path to the installation directory
- The execution on a Jenkins slave requires the adaption of the ECU-TEST installation directory on the slave configuration page

### TEST-GUIDE configuration
- TEST-GUIDE is also configured in the Jenkins system configuration at section "TEST-GUIDE"
- A new entry requires both an arbitrary name and the selection of a previously configured ECU-TEST installation
- Further settings like the server or ATX specific configuration can be found in the advanced block

### Job configuration
The plugin provides several new build steps:

![Build steps](/doc/build_steps.png)

**Note:**
- All build steps have an "Advanced" button through which more options can be accessed
- All text fields can be parameterized which are resolved at build execution
  - Syntax: *$parameter* or *${parameter}*
  
The post-build actions are extended by these test report related post-build steps:

![Build steps](/doc/post_build_steps.png)

## Job execution

- After job configuration is done a new build can be triggerd by clicking on "Build now" button
- The console log contains detailed information per build step execution
- The test results are published both on the build page and the job page based on the latest build

## Screencasts
For more information about installation and configuration of the plug-in as well as using the plug-in in a slave based setup see our [Screencasts](https://www.tracetronic.com/products/ecu-test/jenkins/).

## License
The ECU-TEST Jenkins plug-in is licensed under 3-clause BSD license.

More information can be found inside the LICENSE file.
