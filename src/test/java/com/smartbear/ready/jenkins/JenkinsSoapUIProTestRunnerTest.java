package com.smartbear.ready.jenkins;

import hudson.util.FormValidation;
import hudson.util.FormValidation.Kind;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class JenkinsSoapUIProTestRunnerTest {

    private static JenkinsSoapUIProTestRunner.DescriptorImpl validationService;
    private static final String CLIENT_CREDENTIALS_AUTH_METHOD = AuthMethod.CLIENT_CREDENTIALS.getDisplayName();

    @BeforeClass
    public static void setUp() {
        validationService = new JenkinsSoapUIProTestRunner.DescriptorImpl();
    }

    @Test
    public void validateEmptySlmLicenceApiHostForAccessForEveryoneMethodTest() {
        // given
        final String authMethod = AuthMethod.ACCESS_FOR_EVERYONE.getDisplayName();

        // when
        final FormValidation result = validationService.doCheckSlmLicenceApiHost("", authMethod);

        // then
        assertThat(result.kind, is(Kind.ERROR));
    }

    @Test
    public void validateEmptySlmLicenceApiPortForAccessForEveryoneMethodTest() {
        // given
        final String authMethod = AuthMethod.ACCESS_FOR_EVERYONE.getDisplayName();

        // when
        final FormValidation result = validationService.doCheckSlmLicenceApiPort("", authMethod);

        // then
        assertThat(result.kind, is(Kind.ERROR));
    }

    @Test
    public void validateEmptySlmLicenceAccessKeyForApiKeyMethodTest() {
        // given
        final String authMethod = AuthMethod.API_KEY.getDisplayName();

        // when
        final FormValidation result = validationService.doCheckSlmLicenceAccessKey("", authMethod);

        // then
        assertThat(result.kind, is(Kind.ERROR));
    }

    @Test
    public void validateEmptyClientIdForClientCredentialsMethodTest() {
        // when
        final FormValidation result = validationService.doCheckSlmLicenseClientId("", CLIENT_CREDENTIALS_AUTH_METHOD);

        // then
        assertThat(result.kind, is(Kind.ERROR));
    }

    @Test
    public void validateEmptyClientSecretForClientCredentialsMethodTest() {
        // when
        final FormValidation result = validationService.doCheckSlmLicenseClientSecret("", CLIENT_CREDENTIALS_AUTH_METHOD);

        // then
        assertThat(result.kind, is(Kind.ERROR));
    }

    @Test
    public void validateEmptySlmLicenceApiHostForClientCredentialsMethodTest() {
        // when
        final FormValidation result = validationService.doCheckSlmLicenceApiHost("", CLIENT_CREDENTIALS_AUTH_METHOD);

        // then
        assertThat(result.kind, is(Kind.ERROR));
    }

    @Test
    public void validateEmptySlmLicenceApiPortForClientCredentialsMethodTest() {
        // when
        final FormValidation result = validationService.doCheckSlmLicenceApiPort("", CLIENT_CREDENTIALS_AUTH_METHOD);

        // then
        assertThat(result.kind, is(Kind.ERROR));
    }

    @Test
    public void validateStringSlmLicenceApiPortForClientCredentialsMethodTest() {
        // when
        final FormValidation result = validationService.doCheckSlmLicenceApiPort("abc", CLIENT_CREDENTIALS_AUTH_METHOD);

        // then
        assertThat(result.kind, is(Kind.ERROR));
    }

    @Test
    public void validateTooLargeSlmLicenceApiPortForClientCredentialsMethodTest() {
        // when
        final FormValidation result = validationService.doCheckSlmLicenceApiPort("65536", CLIENT_CREDENTIALS_AUTH_METHOD);

        // then
        assertThat(result.kind, is(Kind.ERROR));
    }

    @Test
    public void validateNegativeSlmLicenceApiPortForClientCredentialsMethodTest() {
        // when
        final FormValidation result = validationService.doCheckSlmLicenceApiPort("-8080", CLIENT_CREDENTIALS_AUTH_METHOD);

        // then
        assertThat(result.kind, is(Kind.ERROR));
    }

    @Test
    public void validateProperSlmLicenceApiPortForClientCredentialsMethodTest() {
        // when
        final FormValidation result = validationService.doCheckSlmLicenceApiPort("8080", CLIENT_CREDENTIALS_AUTH_METHOD);

        // then
        assertThat(result.kind, is(Kind.OK));
    }

}