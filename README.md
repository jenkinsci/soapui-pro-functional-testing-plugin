# ReadyAPI Test Functional Testing Plugin

### About

A SmartBear plugin used to run ReadyAPI functional tests from Jenkins builds. 

### Requirements

* The project you want to run must be saved in ReadyAPI version 2.1.0 or later.
* The Jenkins node where you want to run your test must have ReadyAPI installed with an activated ReadyAPI Test license. You can install and activate the license from a Jenkins build. To learn how to do that, see [ReadyAPI documentation](https://support.smartbear.com/readyapi/docs/soapui/running/automating/jenkins.html).

### Important Note

Make sure you run Jenkins under the same user account you used to activate the ReadyAPI Test license. Otherwise, you will get the "License not found" error. To learn how to fix this issue, see [ReadyAPI documentation](https://support.smartbear.com/readyapi/docs/general-info/licensing/troubleshooting/jenkins.html).

### Configuration

The build step has the following settings:  
	
* **Path to testrunner** - Specifies the fully-qualified path to the runner file (*testrunner.bat* or *testrunner.sh*). By default, you can find it in the *<ReadyAPI installation>/bin* directory.
* **Path to ReadyAPI project** -  Specifies the fully-qualified path to the ReadyAPI project you want to run.
* **Test Suite** - Specifies the test suite to run. To run all the test suites of your project, leave the field blank.
* **Test Case** - Specifies the test case to run. If you leave the field blank, the runner will execute all the test cases of the specified test suite, or, if you have not specified a test suite, all the test cases of your project.
* **Test Suite Tags** and **Test Case Tags** - Specify which tags must contain the test suite or test case to be run. To create complex conditions, use the `||` (logical OR), `&&` (logical AND) and `!` (logical NOT) operators.
* **Project Password** - Specifies the encryption password, if you encrypted the entire project or some of its custom properties.
* **Environment** - Specifies the environment configuration for the test run.

### Reports

After the build is over, the plugin creates the following reports:

*	A [printable PDF report](https://support.smartbear.com/readyapi/docs/testing/reports/getting-started.html), which is published to Jenkins. To view it, open the build page you are interested in and select **ReadyAPI Test Results** on the left. Also, the link to the report is available at the end of the **Console Output** log.
*	A [JUnit-Style HTML report](https://support.smartbear.com/readyapi/docs/testing/reports/existing/html.html), which is available in the Jenkins workspace directory.
*	A JUnit report, which is published to Jenkins. To view it, open the build page you are interested in and select **Test Results** on the left.

### More information

You can find more information on how to use the plugin in [ReadyAPI documentation](https://support.smartbear.com/readyapi/docs/soapui/running/automating/jenkins.html).

### Version history

#### Version 1.7 (July 21, 2021)

* *New feature*: Added the possibility to use EnvVars for TestRunner and project file paths.
* *Fixed*: Project execution sometimes hung on a slave machine.

#### Version 1.5 (June 24, 2020)

* *Fixed*: The test case report could not be generated when a single test case was run.

#### Version 1.4 (April 10, 2020)

* *Fixed*: A security vulnerability in project password storage. 

If you update to version 1.4, to ensure the security of your passwords, you need to do the following for all the jobs that use the plugin:

1. Select a job and click **Configure**. 
2. Save the configuration without making any changes by clicking **Save** or **Apply**.

#### Version 1.3 (February 7, 2020)

* *Fixed*: In some cases, a job with a composite project could not be run.

#### Version 1.2 (December 13, 2019)

* *New feature*: Support for [tags](https://support.smartbear.com/readyapi/docs/soapui/ui/project.html#tags).

#### Version 1.1 (August 7, 2018)

* *New feature*: A printable PDF report is now published to Jenkins.
* *Fixed*: When a test failed, the build was not marked as failed.
* *Fixed*: JUnit reports were not published if the build was run on a slave computer.
* *Fixed*: The optional fields were required in the Blue Ocean UI.

#### Version 1.0 (June 5, 2018)

* Initial release.
