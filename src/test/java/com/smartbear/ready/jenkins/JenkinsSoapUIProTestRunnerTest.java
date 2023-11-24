package com.smartbear.ready.jenkins;

import hudson.util.FormValidation;
import hudson.util.FormValidation.Kind;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class JenkinsSoapUIProTestRunnerTest {

    private static JenkinsSoapUIProTestRunner.DescriptorImpl validationService;

    @BeforeClass
    public static void setUp() {
        validationService = new JenkinsSoapUIProTestRunner.DescriptorImpl();
    }

    @Test
    public void validateEmptySlmLicenceApiHostForUserAndPasswordMethodTest() {
        // given
        final String authMethod = "USER_AND_PASSWORD";

        // when
        final FormValidation result = validationService.doCheckSlmLicenceApiHost("", authMethod);

        // then
        assertThat(result.kind, is(Kind.ERROR));
    }

    @Test
    public void validateEmptySlmLicenceApiHostForAccessForEveryoneMethodTest() {
        // given
        final String authMethod = "ACCESS_FOR_EVERYONE";

        // when
        final FormValidation result = validationService.doCheckSlmLicenceApiHost("", authMethod);

        // then
        assertThat(result.kind, is(Kind.ERROR));
    }

    @Test
    public void validateEmptySlmLicenceApiHostForFileBasedMethodTest() {
        // given
        final String authMethod = "FILE_BASED";

        // when
        final FormValidation result = validationService.doCheckSlmLicenceApiHost("", authMethod);

        // then
        assertThat(result.kind, is(Kind.OK));
    }

    @Test
    public void validateEmptySlmLicenceApiPortForUserAndPasswordMethodTest() {
        // given
        final String authMethod = "USER_AND_PASSWORD";

        // when
        final FormValidation result = validationService.doCheckSlmLicenceApiPort("", authMethod);

        // then
        assertThat(result.kind, is(Kind.ERROR));
    }

    @Test
    public void validateEmptySlmLicenceApiPortForAccessForEveryoneMethodTest() {
        // given
        final String authMethod = "ACCESS_FOR_EVERYONE";

        // when
        final FormValidation result = validationService.doCheckSlmLicenceApiPort("", authMethod);

        // then
        assertThat(result.kind, is(Kind.ERROR));
    }

    @Test
    public void validateEmptySlmLicenceApiPortForFileBasedMethodTest() {
        // given
        final String authMethod = "FILE_BASED";

        // when
        final FormValidation result = validationService.doCheckSlmLicenceApiPort("", authMethod);

        // then
        assertThat(result.kind, is(Kind.OK));
    }

    @Test
    public void validateStringSlmLicenceApiPortForUserAndPasswordMethodTest() {
        // given
        final String authMethod = "USER_AND_PASSWORD";
        final String port = "abc";

        // when
        final FormValidation result = validationService.doCheckSlmLicenceApiPort(port, authMethod);

        // then
        assertThat(result.kind, is(Kind.ERROR));
    }

    @Test
    public void validateTooLargeSlmLicenceApiPortForUserAndPasswordMethodTest() {
        // given
        final String authMethod = "USER_AND_PASSWORD";
        final String port = "88888";

        // when
        final FormValidation result = validationService.doCheckSlmLicenceApiPort(port, authMethod);

        // then
        assertThat(result.kind, is(Kind.ERROR));
    }

    @Test
    public void validateNegativeSlmLicenceApiPortForUserAndPasswordMethodTest() {
        // given
        final String authMethod = "USER_AND_PASSWORD";
        final String port = "-12";

        // when
        final FormValidation result = validationService.doCheckSlmLicenceApiPort(port, authMethod);

        // then
        assertThat(result.kind, is(Kind.ERROR));
    }

    @Test
    public void validateProperSlmLicenceApiPortForUserAndPasswordMethodTest() {
        // given
        final String authMethod = "USER_AND_PASSWORD";
        final String port = "8080";

        // when
        final FormValidation result = validationService.doCheckSlmLicenceApiPort(port, authMethod);

        // then
        assertThat(result.kind, is(Kind.OK));
    }

    @Test
    public void validateEmptySlmLicenceAccessKeyForApiKeyMethodTest() {
        // given
        final String authMethod = "API_KEY";

        // when
        final FormValidation result = validationService.doCheckSlmLicenceAccessKey("", authMethod);

        // then
        assertThat(result.kind, is(Kind.ERROR));
    }

    @Test
    public void validateEmptyUsernameForUserAndPasswordMethodTest() {
        // given
        final String authMethod = "USER_AND_PASSWORD";

        // when
        final FormValidation result = validationService.doCheckUser("", authMethod);

        // then
        assertThat(result.kind, is(Kind.ERROR));
    }

    @Test
    public void validateEmptyPasswordForUserAndPasswordMethodTest() {
        // given
        final String authMethod = "USER_AND_PASSWORD";

        // when
        final FormValidation result = validationService.doCheckPassword("", authMethod);

        // then
        assertThat(result.kind, is(Kind.ERROR));
    }

    @Test
    public void validateOnPremServerForUserAndPasswordMethodTest() {
        // given
        final String authMethod = "USER_AND_PASSWORD";
        final String slmLicenseApiHost = "localhost";

        // when
        final FormValidation result = validationService.doCheckAuthMethod(authMethod, slmLicenseApiHost);

        // then
        assertThat(result.kind, is(Kind.OK));
    }

    @Test
    public void validateSmartBearHostedServerForUserAndPasswordMethodTest() {
        // given
        final String authMethod = "USER_AND_PASSWORD";
        final String slmLicenseApiHost = "";

        // when
        final FormValidation result = validationService.doCheckAuthMethod(authMethod, slmLicenseApiHost);

        // then
        assertThat(result.kind, is(Kind.ERROR));
    }
}