# Changelog (1.0 - 2.8)

All notable changes of version 1.0 - 2.8 are documented below.

All future changes will be automatically logged by ![Release Drafter Icon](https://avatars3.githubusercontent.com/in/14356?s=12&v=4) [Release Drafter](https://github.com/apps/release-drafter) in [GitHub Releases](https://github.com/jenkinsci/ecutest-plugin/releases).

## [2.8](https://github.com/jenkinsci/ecutest-plugin/compare/ecutest-2.7...ecutest-2.8) (May 24, 2019)

- :heavy_plus_sign: Check whether configurations are started (PR #32)
- :heavy_plus_sign: Add timeout option using Quit and Exit via COM API (PR #34)
- :heavy_plus_sign: Add more debug logs (PR #37)
- :information_source: Ensure compatibility up to ECU-TEST 8.0.0 (PR #35)
- :information_source: Drop support for ECU-TEST 6.x (PR #35)
- :information_source: Update available ATX settings to TEST-GUIDE 1.67.0 (PR #36)

## [2.7](https://github.com/jenkinsci/ecutest-plugin/compare/ecutest-2.6...ecutest-2.7) (May 17, 2019)

- :heavy_plus_sign: Add full support for [Configuration as Code](https://plugins.jenkins.io/configuration-as-code) plugin (PR #23)
- :heavy_plus_sign: Allow dynamic inline ECU-TEST instances in pipelines (PR #31)
- :heavy_plus_sign: Allow dynamic inline TEST-GUIDE instances in pipelines (PR #29)
- :heavy_check_mark: Use random generated id for linking reports (PR #24)

## [2.6](https://github.com/jenkinsci/ecutest-plugin/compare/ecutest-2.5...ecutest-2.6) (Feb 15, 2019)

- :heavy_plus_sign: Add option to update user libraries (PR #12)
- :heavy_plus_sign: Support generation and re-use of ECU-TEST caches (PR #13)
- :heavy_plus_sign: Allow individual Windows task name (PR #19)
- :heavy_check_mark: Request API endpoint to check TEST-GUIDE availability (PR #15)
- :heavy_check_mark: Internal refactoring (PR #7)
- :x: Reference filter validation throws ANTLR exception (PR #17)
- :information_source: Updated available ATX settings to TEST-GUIDE 1.64.1
- :information_source: Requires at least TEST-GUIDE 1.55.0

## [2.5](https://github.com/jenkinsci/ecutest-plugin/compare/ecutest-2.4...ecutest-2.5) (Dec 07, 2018)

- :heavy_plus_sign: Allow dynamic TEST-GUIDE configuration in Pipelines
- :heavy_plus_sign: Add partial [Configuration as Code](https://plugins.jenkins.io/configuration-as-code) plugin support to manage ECU-TEST installations
- :heavy_check_mark: Use unique ATX id for linking reports to TEST-GUIDE if available
- :heavy_check_mark: Improved ECU-TEST version parsing (PR #6)
- :information_source: Ensured compatibility up to ECU-TEST 7.2.0

## [2.4](https://github.com/jenkinsci/ecutest-plugin/compare/ecutest-2.3...ecutest-2.4) (Jul 30, 2018)

- :x: Fixed security issues [SECURITY-932](https://jenkins.io/security/advisory/2018-07-30/#SECURITY-932), [SECURITY-994](https://jenkins.io/security/advisory/2018-07-30/#SECURITY-994)

## [2.3](https://github.com/jenkinsci/ecutest-plugin/compare/ecutest-2.2...ecutest-2.3) (Jul 20, 2018)

- :heavy_check_mark: Allow to use TMS credentials from any Jenkins context
- :heavy_check_mark: Removed existence check of .workspace directory
- :heavy_check_mark: Improved COM connection stability by updating to JACOB 1.19
- :heavy_check_mark: More robust parsing the ATX upload response
- :information_source: Updated available ATX settings to TEST-GUIDE 1.57.1

## [2.2](https://github.com/jenkinsci/ecutest-plugin/compare/ecutest-2.1...ecutest-2.2) (Apr 13, 2018)

- :heavy_check_mark: Allow additional data types as global constant
- :heavy_check_mark: Improved COM connection stability with COM timeouts
- :heavy_check_mark: Evaluate ATX upload info for linking ATX reports
- :information_source: Ensured compatibility up to ECU-TEST 7.1.0
- :information_source: Updated available ATX settings to TEST-GUIDE 1.54.0

## [2.1](https://github.com/jenkinsci/ecutest-plugin/compare/ecutest-2.0...ecutest-2.1) (Feb 16, 2018)

- :heavy_plus_sign: Added publisher for downstream trace analysis
- :heavy_check_mark: Disabled COM timeout by default
- :x: Fixed ATX report links when using downstream publisher
- :information_source: Bumped Jenkins baseline to LTS 2.60.3

## [2.0](https://github.com/jenkinsci/ecutest-plugin/compare/ecutest-2.0...ecutest-1.18) (Dec 08, 2017)

- :heavy_plus_sign: Added publisher for downstream report generation
- :heavy_plus_sign: Added build variables containing last loaded ECU-TEST configurations
- :heavy_plus_sign: Added COM timeout to detect non-responding ECU-TEST instance or tool connections
- :x: Fixed sporadic errors when setting global constants
- :information_source: Bumped Jenkins baseline to LTS 2.60.1
- :information_source: Requires Java 8 on master and slaves
- :information_source: Ensured compatibility up to ECU-TEST 7.0.0
- :information_source: Updated available ATX settings to TEST-GUIDE 1.49.0

## [1.18](https://github.com/jenkinsci/ecutest-plugin/compare/ecutest-1.17.1...ecutest-1.18) (Jul 28, 2017)

- :heavy_plus_sign: Added option to re-use ECU-TEST and Tool-Server instances
- :heavy_check_mark: Ignore ECU-TEST patch version in compatibility checks
- :heavy_check_mark: Check architecture compatibility between ECU-TEST and JVM
- :x: Ignore test configuration validation if previous ones re-used
- :information_source: Updated available ATX settings to TEST-GUIDE 1.46.0

## [1.17.1](https://github.com/jenkinsci/ecutest-plugin/compare/ecutest-1.17...ecutest-1.17.1) (Jun 06, 2017)

- :x: Fixed duplicate test project action

## [1.17](https://github.com/jenkinsci/ecutest-plugin/compare/ecutest-1.16...ecutest-1.17) (Jun 02, 2017)

- :heavy_plus_sign: Added option to keep currently loaded configurations
- :heavy_plus_sign: Added option to import missing packages for project imports
- :heavy_plus_sign: Added build step to import and export packages/projects from/to test management system
- :heavy_plus_sign: Added build step to import and export package/project attributes from/to test management system
- :heavy_plus_sign: Added appropriate [Job DSL](https://plugins.jenkins.io/job-dsl) and [Pipeline](https://plugins.jenkins.io/workflow-aggregator) plugin support for for test management build steps
- :heavy_check_mark: Keep test results in case of manual build abort or exceeded timeout
- :heavy_check_mark: Debug log used COM ProgId and version (use system property ecutest.debugLog=<true|false>)
- :heavy_check_mark: Retain backward compatibility for legacy test management builders
- :information_source: Ensured compatibility up to ECU-TEST 6.6.0
- :information_source: Updated available ATX settings to TEST-GUIDE 1.44.0

## [1.16](https://github.com/jenkinsci/ecutest-plugin/compare/ecutest-1.16...ecutest-1.17) (Mar 10, 2017)

- :heavy_plus_sign: Improved summary view for tool and test related build variables
- :heavy_plus_sign: Added support for multiple TEST-GUIDE projects
- :heavy_check_mark: Append to existing JUnit results to prevent duplicated trend graphs
- :heavy_check_mark: Exclude job analysis files when traversing sub reports
- :heavy_check_mark: Compatibility changes for TEST-GUIDE 1.39+
- :information_source: Updated available ATX settings to TEST-GUIDE 1.41.0

## [1.15](https://github.com/jenkinsci/ecutest-plugin/compare/ecutest-1.14...ecutest-1.15) (Jan 06, 2017)

- :heavy_plus_sign: Added build step to import projects from an archive or a test management system (like RQM, ALM)
- :heavy_plus_sign: Added post-build step allowing to export test results to a test management system
- :heavy_plus_sign: Added appropriate [Job DSL](https://plugins.jenkins.io/job-dsl) and [Pipeline](https://plugins.jenkins.io/workflow-aggregator) plugin support for new features above
- :heavy_check_mark: Depend on [Icon Shim](https://plugins.jenkins.io/icon-shim) plugin to simplify icon usage (JENKINS-36472)
- :x: Fixed ATX report links for long-running uploads

## [1.14](https://github.com/jenkinsci/ecutest-plugin/compare/ecutest-1.13...ecutest-1.14) (Nov 25, 2016)

- :heavy_check_mark: Remove empty installations when saving configurations
- :heavy_check_mark: Avoid circular dependency warning on Jenkins startup ([JENKINS-39169](https://issues.jenkins-ci.org/browse/JENKINS-39169))
- :x: Fixed TRF include/exclude pattern for report publishers
- :x: Fixed ATX report links for sub project execution
- :information_source: Updated available ATX settings to TEST-GUIDE 1.38.0

## [1.13](https://github.com/jenkinsci/ecutest-plugin/compare/ecutest-1.12...ecutest-1.13) (Oct 14, 2016)

- :heavy_plus_sign: Added [@Symbol](https://github.com/jenkinsci/pipeline-plugin/blob/master/DEVGUIDE.md#defining-symbols) annotation to simplify pipeline syntax
- :x: Exclude job analysis reports from publishing

## [1.12](https://github.com/jenkinsci/ecutest-plugin/compare/ecutest-1.11...ecutest-1.12) (Sep 16, 2016)

- :heavy_plus_sign: Added support for new TRF naming convention (like package/project name)
- :heavy_plus_sign: Added support for changed installation path of Tool-Server
- :heavy_plus_sign: Added support for parallel installations (configurable COM ProgID)
- :heavy_check_mark: Properly log ATX report upload errors
- :heavy_check_mark: Simplified configuration with automatic migration
- :heavy_check_mark: Improved Windows process control (using [WinP](http://winp.kohsuke.org/))
- :x: Fixed absolute configuration path check
- :information_source: Ensured compatibility up to ECU-TEST 6.5.0
- :information_source: Updated available ATX settings to TEST-GUIDE 1.35.0

## [1.11](https://github.com/jenkinsci/ecutest-plugin/compare/ecutest-1.10...ecutest-1.11) (May 27, 2016)

- :heavy_plus_sign: Added support for [Pipeline](https://plugins.jenkins.io/workflow-aggregator) plugin (fka Workflow) ([JENKINS-31999](https://issues.jenkins-ci.org/browse/JENKINS-31999))
- :information_source: Bumped Jenkins baseline to LTS 1.580.1

## [1.10](https://github.com/jenkinsci/ecutest-plugin/compare/ecutest-1.9...ecutest-1.10) (May 13, 2016)

- :heavy_plus_sign: Added option to publish and parse test specific log files
- :heavy_check_mark: Allow use of parameterized tool selection in Job DSL extensions
- :information_source: Bumped to new parent POM 2.9
- :information_source: Updated available ATX settings to TEST-GUIDE 1.30.0

## [1.9](https://github.com/jenkinsci/ecutest-plugin/compare/ecutest-1.8...ecutest-1.9) (Apr 15, 2016)

- :heavy_plus_sign: Added publishing option to disable archiving reports
- :heavy_plus_sign: Added option to keep artifacts of most recent build only (project level)
- :information_source: Ensured compatibility up to ECU-TEST 6.4.0
- :information_source: Updated available ATX settings to TEST-GUIDE 1.29.0

## [1.8](https://github.com/jenkinsci/ecutest-plugin/compare/ecutest-1.7...ecutest-1.8) (Mar 18, 2016)

- :heavy_plus_sign: Added workaround for already loaded JACOB library ([JENKINS-31961](https://issues.jenkins-ci.org/browse/JENKINS-31961))
- :heavy_plus_sign: Added report publisher allowing to execute report generators
- :x: Fixed test execution handling when aborting the build ([JENKINS-33457](https://issues.jenkins-ci.org/browse/JENKINS-33457))

## [1.7](https://github.com/jenkinsci/ecutest-plugin/compare/ecutest-1.6...ecutest-1.7) (Mar 04, 2016)

- :heavy_plus_sign: Added parameterized ECU-TEST and TEST-GUIDE selection
- :heavy_plus_sign: Allow use of parameterized ATX settings
- :heavy_check_mark: Improved test execution timeout handling
- :information_source: Ensured compatibility up to ECU-TEST 6.3.2
- :information_source: Updated available ATX settings to TEST-GUIDE 1.27.0

## [1.6](https://github.com/jenkinsci/ecutest-plugin/compare/ecutest-1.5...ecutest-1.6) (Jan 29, 2016)

- :heavy_plus_sign: Added option to only load the configurations without starting them automatically
- :heavy_check_mark: Check for existing global constants, prevents to unnecessarily reload the configurations

## [1.5](https://github.com/jenkinsci/ecutest-plugin/compare/ecutest-1.4...ecutest-1.5) (Dec 18, 2015)

- :heavy_plus_sign: Added option to explicitly set the ECU-TEST settings directory
- :heavy_check_mark: Allow use of GStrings in Job DSL extensions
- :heavy_check_mark: Discard empty global constants and package parameters
- :x: Fixed undetected ATX upload errors ([JENKINS-32078](https://issues.jenkins-ci.org/browse/JENKINS-32078))

## [1.4](https://github.com/jenkinsci/ecutest-plugin/compare/ecutest-1.3...ecutest-1.4) (Dec 04, 2015)

- :heavy_plus_sign: Added support to pre-check packages and projects
- :heavy_plus_sign: Added option to force reloading the current configuration
- :heavy_check_mark: Highlighted the test result in the console log
- :x: Fixed ATX report links for multiple archived ATX files ([JENKINS-31770](https://issues.jenkins-ci.org/browse/JENKINS-31770))

## [1.3](https://github.com/jenkinsci/ecutest-plugin/compare/ecutest-1.2...ecutest-1.3) (Nov 23, 2015)

- :heavy_plus_sign: Added post-execution timeouts
- :heavy_check_mark: Improved Job DSL extensions by providing default settings
- :x: Fixed form validation for some ATX settings ([JENKINS-31703](https://issues.jenkins-ci.org/browse/JENKINS-31703))

## [1.2](https://github.com/jenkinsci/ecutest-plugin/compare/ecutest-1.1...ecutest-1.2) (Nov 06, 2015)

- :heavy_plus_sign: Added support for [Job DSL](https://plugins.jenkins.io/job-dsl) plugin
- :heavy_plus_sign: Added ZIP download link for published TRF and ATX reports
- :heavy_check_mark: Aggregated UNIT test results for matrix projects

## [1.1](https://github.com/jenkinsci/ecutest-plugin/compare/ecutest-1.0...ecutest-1.1) (Oct 23, 2015)

- :heavy_plus_sign: Added support for separate sub-project execution
- :heavy_plus_sign: Added archiving of ATX reports for disabled ATX upload
- :heavy_check_mark: Improved publisher behavior handling ECU-TEST instances
- :heavy_check_mark: Improved ECU-TEST log parser performance
- :heavy_check_mark: Improved validation of TEST-GUIDE server availability

## 1.0 (Oct 09, 2015)

- :information_source: Initial release
