package com.here.platform.proxy.admin.provider;

import com.here.platform.ns.dto.SentryErrorsList;
import com.here.platform.proxy.BaseProxyTests;
import com.here.platform.proxy.dto.AwsS3Provider;
import com.here.platform.proxy.dto.AwsS3ProviderEnum;
import com.here.platform.proxy.conrollers.ServiceProvidersController;
import com.here.platform.proxy.dto.ProxyErrorList;
import com.here.platform.proxy.dto.ProxyProvider;
import com.here.platform.proxy.dto.ProxyProvider.CredentialsAuthMethod;
import com.here.platform.proxy.dto.ProxyProviderResource;
import com.here.platform.proxy.dto.ProxyProviderResourceEnum;
import com.here.platform.proxy.dto.ProxyProviderEnum;
import com.here.platform.proxy.helper.ProxyProviderAssertion;
import com.here.platform.proxy.steps.ProxySteps;
import java.util.List;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("Proxy Admin")
@Tag("proxy_admin")
@DisplayName("[External Proxy] Verify Service Providers Add New")
public class ProviderAdd extends BaseProxyTests {

    @Test
    @DisplayName("[External Proxy] Add new Service Provider")
    void verifyAddProxyProvider() {
        ProxyProvider proxyProvider = ProxyProviderEnum.generate();

        var response = new ServiceProvidersController()
                .withAdminToken()
                .addProvider(proxyProvider);
        new ProxyProviderAssertion(response)
                .expectedCode(HttpStatus.SC_OK)
                .expectedEqualsProvider(proxyProvider);
    }

    @Test
    @DisplayName("[External Proxy] Add new Service Provider No Token")
    void verifyAddProxyProviderNoToken() {
        ProxyProvider proxyProvider = ProxyProviderEnum.generate();

        var response = new ServiceProvidersController()
                .addProvider(proxyProvider);
        new ProxyProviderAssertion(response)
                .expectedSentryError(SentryErrorsList.TOKEN_NOT_FOUND.getError());
    }

    @Test
    @DisplayName("[External Proxy] Add new Service Provider same domain and name")
    void verifyAddProxyProviderSameData() {
        ProxyProvider proxyProvider = ProxyProviderEnum.generate();

        var response = new ServiceProvidersController()
                .withAdminToken()
                .addProvider(proxyProvider);
        new ProxyProviderAssertion(response)
                .expectedCode(HttpStatus.SC_OK);

        var response2 = new ServiceProvidersController()
                .withAdminToken()
                .addProvider(proxyProvider);
        new ProxyProviderAssertion(response2)
                .expectedCode(HttpStatus.SC_CONFLICT);
    }

    @Test
    @DisplayName("[External Proxy] Add new Service Provider same domain")
    void verifyAddProxyProviderSameDomain() {
        ProxyProvider proxyProvider = ProxyProviderEnum.generate();
        ProxyProvider proxyProvider2 = ProxyProviderEnum.generate();
        proxyProvider2.setIdentifier(proxyProvider.getIdentifier());

        var response = new ServiceProvidersController()
                .withAdminToken()
                .addProvider(proxyProvider);
        new ProxyProviderAssertion(response)
                .expectedCode(HttpStatus.SC_OK);

        var response2 = new ServiceProvidersController()
                .withAdminToken()
                .addProvider(proxyProvider2);
        new ProxyProviderAssertion(response2)
                .expectedCode(HttpStatus.SC_CONFLICT);
    }

    @Test
    @DisplayName("[External Proxy] Add new Service Provider same name")
    void verifyAddProxyProviderSameName() {
        ProxyProvider proxyProvider = ProxyProviderEnum.generate();
        ProxyProvider proxyProvider2 = ProxyProviderEnum.generate();
        proxyProvider2.setServiceName(proxyProvider.getServiceName());

        var response = new ServiceProvidersController()
                .withAdminToken()
                .addProvider(proxyProvider);
        new ProxyProviderAssertion(response)
                .expectedCode(HttpStatus.SC_OK);

        var response2 = new ServiceProvidersController()
                .withAdminToken()
                .addProvider(proxyProvider2);
        new ProxyProviderAssertion(response2)
                .expectedCode(HttpStatus.SC_CONFLICT);
    }

    @Test
    @DisplayName("[External Proxy] Add new Service Provider Not implemented Auth")
    void verifyAddProxyProviderNotImplemented() {
        ProxyProvider proxyProvider = ProxyProviderEnum.generate()
                .withAuthMethod(CredentialsAuthMethod.BASIC_AUTH,
                        "root", "qwerty");

        var response = new ServiceProvidersController()
                .withAdminToken()
                .addProvider(proxyProvider);
        new ProxyProviderAssertion(response)
                .expectedCode(HttpStatus.SC_OK)
                .expectedEqualsProvider(proxyProvider);
    }

    @Test
    @DisplayName("[External Proxy] Add new Service Provider Header Auth")
    void verifyAddProxyProviderHeaderAuth() {
        ProxyProvider proxyProvider = ProxyProviderEnum.generate()
                .withAuthMethod(CredentialsAuthMethod.API_KEY_IN_QUERY,
                        "Authorization", "1f8647f3-5f86-4b5e-8687-982fd620ef78");

        var response = new ServiceProvidersController()
                .withAdminToken()
                .addProvider(proxyProvider);
        new ProxyProviderAssertion(response)
                .expectedCode(HttpStatus.SC_OK)
                .expectedEqualsProvider(proxyProvider);
    }

    @Test
    @DisplayName("[External Proxy] Add new Service Provider None Auth")
    void verifyAddProxyProviderNoneAuth() {
        ProxyProvider proxyProvider = ProxyProviderEnum.generate();
        proxyProvider.setAuthMethod(CredentialsAuthMethod.NONE);

        var response = new ServiceProvidersController()
                .withAdminToken()
                .addProvider(proxyProvider);
        new ProxyProviderAssertion(response)
                .expectedCode(HttpStatus.SC_OK)
                .expectedEqualsProvider(proxyProvider);
    }

    @Test
    @Disabled
    @DisplayName("[External Proxy] Add new Service Provider Auth is missing")
    void verifyAddProxyProviderAuthMissing() {
        ProxyProvider proxyProvider = ProxyProviderEnum.generate();
        proxyProvider.setAuthMethod(null);

        var response = new ServiceProvidersController()
                .withAdminToken()
                .addProvider(proxyProvider);
        new ProxyProviderAssertion(response)
                .expectedError(ProxyErrorList.getNotValidField());
    }

    @Test
    @DisplayName("[External Proxy] Add new Service Provider with Resource")
    void verifyAddProxyProviderWithResource() {
        ProxyProvider proxyProvider = ProxyProviderEnum.generate();
        ProxyProviderResource resource = ProxyProviderResourceEnum.generate();
        proxyProvider.setResources(List.of(resource));

        var response = new ServiceProvidersController()
                .withAdminToken()
                .addProvider(proxyProvider);
        new ProxyProviderAssertion(response)
                .expectedCode(HttpStatus.SC_OK)
                .expectedEqualsProvider(proxyProvider);
    }
    
    @Test
    @DisplayName("[External Proxy] Verify if AWS Service Provider can be onboarded with REST provider requested body")
    void verifyAddAwsProviderWithRestProviderRequestedBody() {
        ProxyProvider proxyProvider = ProxyProviderEnum.generateAWS();
        ProxySteps.createProxyProvider(proxyProvider);

        var verifyGetProviderById = new ServiceProvidersController()
                .withAdminToken()
                .getProviderById(proxyProvider.getId());
        new ProxyProviderAssertion(verifyGetProviderById)
                .expectedCode(HttpStatus.SC_OK)
                .expectedEqualsProvider(proxyProvider);
    }

    @Test
    @DisplayName("[External Proxy] Verify if AWS provider type can be onboarded")
    void verifyAddAwsProxyProvider() {
        AwsS3Provider awsS3Provider = AwsS3ProviderEnum.generateAwsProvider();
        ProxySteps.createAWSProxyProvider(awsS3Provider);

        var verifyGetProviderById = new ServiceProvidersController()
                .withAdminToken()
                .getProviderById(awsS3Provider.getId());
        new ProxyProviderAssertion(verifyGetProviderById)
                .expectedCode(HttpStatus.SC_OK)
                .expectedEqualsAwsS3Provider(awsS3Provider);
    }

    @Test
    @DisplayName("[External Proxy] Add new AWS Service Provider Empty Provider Type with REST provider requested body")
    void verifyAddAwsProxyProviderEmptyProviderTypeWithRestProviderRequestedBody() {
        String providerType = "";
        ProxyProvider proxyProvider = ProxyProviderEnum.generateAWS(providerType);

        var response = new ServiceProvidersController()
                .withAdminToken()
                .addProvider(proxyProvider);
        new ProxyProviderAssertion(response)
                .expectedCode(HttpStatus.SC_BAD_REQUEST)
                .expectedError(ProxyErrorList.getWrongPoviderType(providerType));
    }

    @Test
    @DisplayName("[External Proxy] Add new AWS Service Provider Empty Provider Type")
    void verifyAddAwsProxyProviderEmptyProviderType() {
        String providerType = "";
        AwsS3Provider awsS3Provider = AwsS3ProviderEnum.generateAwsProvider(providerType);

        var response = new ServiceProvidersController()
                .withAdminToken()
                .addAwsS3Provider(awsS3Provider);
        new ProxyProviderAssertion(response)
                .expectedCode(HttpStatus.SC_BAD_REQUEST)
                .expectedError(ProxyErrorList.getWrongPoviderType(providerType));
    }

    @Test
    @DisplayName("[External Proxy] Add new AWS Service Provider No Provider Type with REST provider requested body")
    void verifyAddAwsProxyProviderNoProviderTypeWithRestProviderRequestedBody() {
        ProxyProvider proxyProvider = ProxyProviderEnum.generateAWSNoProviderType();

        var response = new ServiceProvidersController()
                .withAdminToken()
                .addProvider(proxyProvider);
        new ProxyProviderAssertion(response)
                .expectedCode(HttpStatus.SC_BAD_REQUEST)
                .expectedError(ProxyErrorList.getNoPoviderType());
    }

    @Test
    @DisplayName("[External Proxy] Add new AWS Service Provider No Provider Type")
    void verifyAddAwsProxyProviderNoProviderType() {
        AwsS3Provider awsS3Provider = AwsS3ProviderEnum.generateAwsProviderNoProviderType();

        var response = new ServiceProvidersController()
                .withAdminToken()
                .addAwsS3Provider(awsS3Provider);
        new ProxyProviderAssertion(response)
                .expectedCode(HttpStatus.SC_BAD_REQUEST)
                .expectedError(ProxyErrorList.getNoPoviderType());
    }

    @Test
    @DisplayName("[External Proxy] Add new AWS Service Provider Non Existent Provider Type with REST provider requested body")
    void verifyAddAwsProxyProviderWrongProviderTypeWithRestProviderRequestedBody() {
        String providerType = "REST";
        ProxyProvider proxyProvider = ProxyProviderEnum.generateAWS(providerType);

        var response = new ServiceProvidersController()
                .withAdminToken()
                .addProvider(proxyProvider);
        new ProxyProviderAssertion(response)
                .expectedCode(HttpStatus.SC_BAD_REQUEST)
                .expectedError(ProxyErrorList.getWrongPoviderType(providerType));
    }

    @Test
    @DisplayName("[External Proxy] Add new AWS Service Provider with Non Existent Provider Type")
    void verifyAddAwsProxyProviderNonExistentProviderType() {
        String providerType = "REST";
        AwsS3Provider awsS3Provider = AwsS3ProviderEnum.generateAwsProvider(providerType);

        var response = new ServiceProvidersController()
                .withAdminToken()
                .addAwsS3Provider(awsS3Provider);
        new ProxyProviderAssertion(response)
                .expectedCode(HttpStatus.SC_BAD_REQUEST)
                .expectedError(ProxyErrorList.getWrongPoviderType(providerType));
    }


}