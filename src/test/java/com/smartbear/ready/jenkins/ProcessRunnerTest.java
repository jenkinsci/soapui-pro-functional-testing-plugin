package com.smartbear.ready.jenkins;

import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.*;

public class ProcessRunnerTest {

    private static ProcessRunner processRunner;
    private static Method testedMethod;

    @BeforeClass
    public static void setUp() throws NoSuchMethodException {
        processRunner = new ProcessRunner();
        testedMethod = ProcessRunner.class
                .getDeclaredMethod("addAuthorisationRelatedParameters", List.class, ParameterContainer.class);
        testedMethod.setAccessible(true);
    }

    @Test
    public void addAuthorisationRelatedParametersForFileBasedAuthMethodTest()
            throws InvocationTargetException, IllegalAccessException {
        // given
        List<String> processParameterList = new ArrayList<>();
        ParameterContainer params = new ParameterContainer.Builder()
                .withAuthMethod("FILE_BASED")
                .build();

        // when
        testedMethod.invoke(processRunner, processParameterList, params);

        // then
        assertThat(processParameterList.size(), is(0));
    }

    @Test
    public void addAuthorisationRelatedParametersForApiKeyAuthMethodTest()
            throws InvocationTargetException, IllegalAccessException {
        // given
        List<String> processParameterList = new ArrayList<>();
        ParameterContainer params = new ParameterContainer.Builder()
                .withAuthMethod("API_KEY")
                .withSlmLicenceApiHost("localhost")
                .withSlmLicenceApiPort("1234")
                .withSlmLicenceAccessKey("SLM_API_KEY")
                .build();

        // when
        testedMethod.invoke(processRunner, processParameterList, params);

        // then
        assertThat(processParameterList.size(), is(4));
        assertThat(processParameterList, hasItems("-DlicenseApiHost=localhost", "-DlicenseApiPort=1234", "-K", "SLM_API_KEY"));
    }

    @Test
    public void addAuthorisationRelatedParametersWithoutHostAndPortForApiKeyAuthMethodTest()
            throws InvocationTargetException, IllegalAccessException {
        // given
        List<String> processParameterList = new ArrayList<>();
        ParameterContainer params = new ParameterContainer.Builder()
                .withAuthMethod("API_KEY")
                .withSlmLicenceAccessKey("SLM_API_KEY")
                .build();

        // when
        testedMethod.invoke(processRunner, processParameterList, params);

        // then
        assertThat(processParameterList.size(), is(2));
        assertThat(processParameterList, hasItems("-K", "SLM_API_KEY"));
    }

    @Test
    public void addAuthorisationRelatedParametersForUserAndPasswordAuthMethodTest()
            throws InvocationTargetException, IllegalAccessException {
        // given
        List<String> processParameterList = new ArrayList<>();
        ParameterContainer params = new ParameterContainer.Builder()
                .withAuthMethod("USER_AND_PASSWORD")
                .withSlmLicenceApiHost("localhost")
                .withSlmLicenceApiPort("1234")
                .withUser("user")
                .withPassword("password")
                .build();

        // when
        testedMethod.invoke(processRunner, processParameterList, params);

        // then
        assertThat(processParameterList.size(), is(6));
        assertThat(processParameterList,
                hasItems("-DlicenseApiHost=localhost", "-DlicenseApiPort=1234", "-U", "user", "-V", "password"));
    }

    @Test
    public void addAuthorisationRelatedParametersWithoutHostAndPortForUserAndPasswordAuthMethodTest()
            throws InvocationTargetException, IllegalAccessException {
        // given
        List<String> processParameterList = new ArrayList<>();
        ParameterContainer params = new ParameterContainer.Builder()
                .withAuthMethod("USER_AND_PASSWORD")
                .withUser("user")
                .withPassword("password")
                .build();

        // when
        testedMethod.invoke(processRunner, processParameterList, params);

        // then
        assertThat(processParameterList.size(), is(4));
        assertThat(processParameterList,
                hasItems("-U", "user", "-V", "password"));
    }

    @Test
    public void addAuthorisationRelatedParametersForAccessForEveryoneAuthMethodTest()
            throws InvocationTargetException, IllegalAccessException {
        // given
        List<String> processParameterList = new ArrayList<>();
        ParameterContainer params = new ParameterContainer.Builder()
                .withAuthMethod("ACCESS_FOR_EVERYONE")
                .withSlmLicenceApiHost("localhost")
                .withSlmLicenceApiPort("1234")
                .build();

        // when
        testedMethod.invoke(processRunner, processParameterList, params);

        // then
        assertThat(processParameterList.size(), is(3));
        assertThat(processParameterList,
                hasItems("-DlicenseApiHost=localhost", "-DlicenseApiPort=1234", "-DlicenseApiAccessForEveryone=true"));
    }

    @Test
    public void addAuthorisationRelatedParametersWithoutHostAndPortForAccessForEveryoneAuthMethodTest()
            throws InvocationTargetException, IllegalAccessException {
        // given
        List<String> processParameterList = new ArrayList<>();
        ParameterContainer params = new ParameterContainer.Builder()
                .withAuthMethod("ACCESS_FOR_EVERYONE")
                .build();

        // when
        testedMethod.invoke(processRunner, processParameterList, params);

        // then
        assertThat(processParameterList.size(), is(1));
        assertThat(processParameterList, hasItems("-DlicenseApiAccessForEveryone=true"));
    }

    @Test
    public void addAuthorisationRelatedParametersForClientCredentialsAuthMethodTest()
            throws InvocationTargetException, IllegalAccessException {
        // given
        List<String> processParameterList = new ArrayList<>();
        ParameterContainer params = new ParameterContainer.Builder()
                .withAuthMethod(AuthMethod.CLIENT_CREDENTIALS.name())
                .withSlmLicenceApiHost("localhost")
                .withSlmLicenceApiPort("1234")
                .withSlmLicenseClientId("testId")
                .withSlmLicenseClientSecret("testSecret")
                .build();

        // when
        testedMethod.invoke(processRunner, processParameterList, params);

        // then
        assertThat(processParameterList.size(), is(6));
        assertThat(processParameterList,
                hasItems("-DlicenseApiHost=localhost", "-DlicenseApiPort=1234", "-ci", "testId", "-cs", "testSecret"));
    }

    @Test
    public void addAuthorisationRelatedParametersWithoutHostAndPortForClientCredentialsAuthMethodTest()
            throws InvocationTargetException, IllegalAccessException {
        // given
        List<String> processParameterList = new ArrayList<>();
        ParameterContainer params = new ParameterContainer.Builder()
                .withAuthMethod(AuthMethod.CLIENT_CREDENTIALS.name())
                .withSlmLicenseClientId("testId")
                .withSlmLicenseClientSecret("testSecret")
                .build();

        // when
        testedMethod.invoke(processRunner, processParameterList, params);

        // then
        assertThat(processParameterList.size(), is(4));
        assertThat(processParameterList,
                hasItems("-ci", "testId", "-cs", "testSecret"));
    }
}