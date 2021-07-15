package com.here.platform.proxy.admin.resource;

import com.here.platform.ns.dto.SentryErrorsList;
import com.here.platform.proxy.BaseProxyTests;
import com.here.platform.proxy.dto.AwsS3Provider;
import com.here.platform.proxy.dto.AwsS3ProviderEnum;
import com.here.platform.proxy.conrollers.ServiceProvidersController;
import com.here.platform.proxy.dto.ProxyErrorList;
import com.here.platform.proxy.dto.ProxyProvider;
import com.here.platform.proxy.dto.ProxyProviderResource;
import com.here.platform.proxy.dto.ProxyProviderResourceEnum;
import com.here.platform.proxy.dto.ProxyProviderEnum;
import com.here.platform.proxy.helper.ProxyProviderAssertion;
import com.here.platform.proxy.steps.ProxySteps;
import io.qameta.allure.Issue;
import java.util.List;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


@Tag("Proxy Admin")
@Tag("proxy_admin")
@DisplayName("[External Proxy] Verify Service Resource Add Resource List")
public class ResourceAdd extends BaseProxyTests {

    @Test
    @DisplayName("[External Proxy] Add Resources to Proxy Provider")
    void verifyAddResourcesToProvider() {
        ProxyProvider proxyProvider = ProxyProviderEnum.generate();
        ProxyProviderResource resource = ProxyProviderResourceEnum.generate();
        ProxySteps.createProxyProvider(proxyProvider);

        var responseRes = new ServiceProvidersController()
                .withAdminToken()
                .addResourceListToProvider(proxyProvider.getId(), resource);
        new ProxyProviderAssertion(responseRes)
                .expectedCode(HttpStatus.SC_OK);
        Long resId = responseRes.getBody().jsonPath().getLong("resources[0].id");
        String resHrn = responseRes.getBody().jsonPath().getString("resources[0].hrn");
        resource.setId(resId);
        resource.setHrn(resHrn);

        new ProxyProviderAssertion(responseRes)
                .expectedResourceInProvider(resource);
    }

    @Test
    @Issue("NS-3668")
    @DisplayName("[External Proxy] Add Resources to Proxy Provider with '/' at the beginning of the path")
    void verifyAddResourcesToProviderWithSlashBeforePath() {
        ProxyProvider proxyProvider = ProxyProviderEnum.generate();
        ProxyProviderResource resource = ProxyProviderResourceEnum.generateResourceWithSlash();
        ProxySteps.createProxyProvider(proxyProvider);

        var responseRes = new ServiceProvidersController()
                .withAdminToken()
                .addResourceListToProvider(proxyProvider.getId(), resource);
        new ProxyProviderAssertion(responseRes)
                .expectedCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    @DisplayName("[External Proxy] Add Resources to Proxy Provider No Token")
    void verifyAddResourcesToProviderNoToken() {
        ProxyProvider proxyProvider = ProxyProviderEnum.generate();
        ProxyProviderResource resource = ProxyProviderResourceEnum.generate();
        ProxySteps.createProxyProvider(proxyProvider);

        var responseRes = new ServiceProvidersController()
                .addResourceListToProvider(proxyProvider.getId(), resource);
        new ProxyProviderAssertion(responseRes)
                .expectedSentryError(SentryErrorsList.TOKEN_NOT_FOUND.getError());
    }

    @Test
    @DisplayName("[External Proxy] Add Resources to Proxy Provider Multiple")
    void verifyAddResourcesToProviderMultiple() {
        ProxyProvider proxyProvider = ProxyProviderEnum.generate();
        ProxyProviderResource resource = ProxyProviderResourceEnum.generate();
        ProxyProviderResource resource2 = ProxyProviderResourceEnum.generate();
        ProxySteps.createProxyProvider(proxyProvider);

        var responseRes = new ServiceProvidersController()
                .withAdminToken()
                .addResourceListToProvider(proxyProvider.getId(), List.of(resource, resource2));
        new ProxyProviderAssertion(responseRes)
                .expectedCode(HttpStatus.SC_OK);
        Long resId = responseRes.getBody().jsonPath().getLong("resources[0].id");
        String resHrn = responseRes.getBody().jsonPath().getString("resources[0].hrn");
        resource.setId(resId);
        resource.setHrn(resHrn);
        Long resId2 = responseRes.getBody().jsonPath().getLong("resources[1].id");
        String resHrn2 = responseRes.getBody().jsonPath().getString("resources[1].hrn");
        resource2.setId(resId2);
        resource2.setHrn(resHrn2);

        new ProxyProviderAssertion(responseRes)
                .expectedCode(HttpStatus.SC_OK)
                .expectedResourceInProvider(resource)
                .expectedResourceInProvider(resource2);
    }

    @Test
    @DisplayName("[External Proxy] Add Resources to Proxy Provider Already Exist")
    void verifyAddResourcesToProviderAlreadyExist() {
        ProxyProvider proxyProvider = ProxyProviderEnum.generate();
        ProxyProviderResource resource = ProxyProviderResourceEnum.generate();
        ProxySteps.createProxyProvider(proxyProvider);

        var responseRes = new ServiceProvidersController()
                .withAdminToken()
                .addResourceListToProvider(proxyProvider.getId(), resource);
        new ProxyProviderAssertion(responseRes)
                .expectedCode(HttpStatus.SC_OK);
        var responseRes2 = new ServiceProvidersController()
                .withAdminToken()
                .addResourceListToProvider(proxyProvider.getId(), resource);

        new ProxyProviderAssertion(responseRes2)
                .expectedError(ProxyErrorList.getProviderResourceAlreadyExistsError(resource.getTitle(), resource.getPath()));
    }

    @Test
    @DisplayName("[External Proxy] Add Resources to Proxy Provider No Provider")
    void verifyAddResourcesToProviderNoProvider() {
        ProxyProviderResource resource = ProxyProviderResourceEnum.generate();

        var responseRes = new ServiceProvidersController()
                .withAdminToken()
                .addResourceListToProvider(-1L, resource);

        new ProxyProviderAssertion(responseRes)
                .expectedError(ProxyErrorList.getProviderNotFoundError(-1L));
    }

    @Test
    @DisplayName("[External Proxy] Add Resources to Proxy Provider with Query")
    void verifyAddResourcesToProviderWithQuery() {
        ProxyProvider proxyProvider = ProxyProviderEnum.generate();
        ProxyProviderResource resource = ProxyProviderResourceEnum.generate();
        resource.setPath(resource.getPath() + "?language=en-uk");
        ProxySteps.createProxyProvider(proxyProvider);

        var responseRes = new ServiceProvidersController()
                .withAdminToken()
                .addResourceListToProvider(proxyProvider.getId(), resource);
        new ProxyProviderAssertion(responseRes)
                .expectedCode(HttpStatus.SC_OK);
        Long resId = responseRes.getBody().jsonPath().getLong("resources[0].id");
        String resHrn = responseRes.getBody().jsonPath().getString("resources[0].hrn");
        resource.setId(resId);
        resource.setHrn(resHrn);

        new ProxyProviderAssertion(responseRes)
                .expectedResourceInProvider(resource);
    }

    @Test
    @Issue("NS-3668")
    @DisplayName("[External Proxy] Verify two resources cannot be added with the same path")
    void verifyTwoProxyResourcesCannotBeAddedWithTheSamePath() {
        ProxyProvider proxyProvider = ProxyProviderEnum.REFERENCE_PROXY.getProxyProvider();
        ProxyProviderResource firstResource = new ProxyProviderResource(
                "Auto-testing-reference-res-1",
                "proxy/data/test");
        ProxyProviderResource secondResource = new ProxyProviderResource(
                "Auto-testing-reference-res-2",
                "proxy/data/test");

        ProxySteps.readProxyProvider(proxyProvider);
        var response = new ServiceProvidersController()
                .withAdminToken()
                .addResourceListToProvider(proxyProvider.getId(), List.of(firstResource, secondResource));
        new ProxyProviderAssertion(response)
                .expectedError(ProxyErrorList.getNotValidFieldNotUniqueResourcePath());
    }

    @Test
    @Issue("NS-3668")
    @DisplayName("[External Proxy] Verify two resources cannot be added with the same Title")
    void verifyTwoProxyResourcesCannotBeAddedWithTheSameTitle() {
        ProxyProvider proxyProvider = ProxyProviderEnum.REFERENCE_PROXY.getProxyProvider();
        ProxyProviderResource firstResource = new ProxyProviderResource(
                "Auto-testing-reference-resource",
                "proxy/data/d-test1");
        ProxyProviderResource secondResource = new ProxyProviderResource(
                "Auto-testing-reference-resource",
                "proxy/data/d-test2");

        ProxySteps.readProxyProvider(proxyProvider);
        var response = new ServiceProvidersController()
                .withAdminToken()
                .addResourceListToProvider(proxyProvider.getId(), List.of(firstResource, secondResource));
        new ProxyProviderAssertion(response)
                .expectedError(ProxyErrorList.getNotValidFieldNotUniqueResourceTitle());
    }

    @Test
    @DisplayName("[External Proxy] Add Resources to AWS Proxy Provider")
    void verifyAddResourcesToAWSProvider() {
        AwsS3Provider awsS3Provider = AwsS3ProviderEnum.generateAwsProvider();
        ProxyProviderResource resource = ProxyProviderResourceEnum.generateAws();
        ProxySteps.createAWSProxyProvider(awsS3Provider);

        var verifyGetProviderById = new ServiceProvidersController()
                .withAdminToken()
                .getProviderById(awsS3Provider.getId());
        new ProxyProviderAssertion(verifyGetProviderById)
                .expectedCode(HttpStatus.SC_OK)
                .expectedEqualsAwsS3Provider(awsS3Provider);

        var responseRes = new ServiceProvidersController()
                .withAdminToken()
                .addResourceListToProvider(awsS3Provider.getId(), resource);
        new ProxyProviderAssertion(responseRes)
                .expectedCode(HttpStatus.SC_OK);
        Long resId = responseRes.getBody().jsonPath().getLong("resources[0].id");
        String resHrn = responseRes.getBody().jsonPath().getString("resources[0].hrn");
        resource.setId(resId);
        resource.setHrn(resHrn);

        new ProxyProviderAssertion(responseRes)
                .expectedResourceInProvider(resource);
    }

    @Test
    @DisplayName("[External Proxy] Verify two resources with the same path cannot be added to AWS Proxy Provider" )
    void verifyTwoAWSProxyResourcesCannotBeAddedWithTheSamePath() {
        AwsS3Provider awsS3Provider = AwsS3ProviderEnum.generateAwsProvider();
        ProxySteps.createAWSProxyProvider(awsS3Provider);

        ProxyProviderResource firstResource = new ProxyProviderResource(
                "Auto-testing-reference-res-1",
                "proxy/dir1/subdir1");
        ProxyProviderResource secondResource = new ProxyProviderResource(
                "Auto-testing-reference-res-2",
                "proxy/dir1/subdir1");

        var response = new ServiceProvidersController()
                .withAdminToken()
                .addResourceListToProvider(awsS3Provider.getId(), List.of(firstResource, secondResource));
        new ProxyProviderAssertion(response)
                .expectedError(ProxyErrorList.getNotValidFieldNotUniqueResourcePath())
                .expectedCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    @DisplayName("[External Proxy] Verify that resource cannot be added to AWS Proxy Provider "
            + "if another resource with the same path already exists" )
    void verifyAWSProxyResourceCannotBeAddedWithExistingPath() {
        AwsS3Provider awsS3Provider = AwsS3ProviderEnum.generateAwsProvider();
        ProxySteps.createAWSProxyProvider(awsS3Provider);

        ProxyProviderResource firstResource = new ProxyProviderResource(
                "Auto-testing-reference-res-1",
                "proxy/dir2/subdir1");
        ProxyProviderResource secondResource = new ProxyProviderResource(
                "Auto-testing-reference-res-2",
                "proxy/dir2/subdir1");

        var response = new ServiceProvidersController()
                .withAdminToken()
                .addResourceListToProvider(awsS3Provider.getId(), firstResource);
        new ProxyProviderAssertion(response)
                .expectedCode(HttpStatus.SC_OK);

        var responseSecond = new ServiceProvidersController()
                .withAdminToken()
                .addResourceListToProvider(awsS3Provider.getId(), secondResource);
        new ProxyProviderAssertion(responseSecond)
                .expectedError(ProxyErrorList.getProviderResourceAlreadyExistsError(
                        "Auto-testing-reference-res-1", "proxy/dir2/subdir1"))
                .expectedCode(HttpStatus.SC_CONFLICT);
    }

    @Test
    @DisplayName("[External Proxy] Verify two resources with the same Title cannot be added to AWS Proxy Provider" )
    void verifyTwoAWSProxyResourcesCannotBeAddedWithTheSameTitle() {
        AwsS3Provider awsS3Provider = AwsS3ProviderEnum.generateAwsProvider();
        ProxySteps.createAWSProxyProvider(awsS3Provider);

        ProxyProviderResource firstResource = new ProxyProviderResource(
                "Auto-testing-reference-resource",
                "proxy/dir21/subdir1");
        ProxyProviderResource secondResource = new ProxyProviderResource(
                "Auto-testing-reference-resource",
                "proxy/dir21/subdir2");

        var response = new ServiceProvidersController()
                .withAdminToken()
                .addResourceListToProvider(awsS3Provider.getId(), List.of(firstResource, secondResource));
        new ProxyProviderAssertion(response)
                .expectedError(ProxyErrorList.getNotValidFieldNotUniqueResourceTitle())
                .expectedCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    @DisplayName("[External Proxy] Verify that resource cannot be added to AWS Proxy Provider "
            + "if another resource with the same Title already exists" )
    void verifyAWSProxyResourceCannotBeAddedWithTheExistingTitle() {
        AwsS3Provider awsS3Provider = AwsS3ProviderEnum.generateAwsProvider();
        ProxySteps.createAWSProxyProvider(awsS3Provider);

        ProxyProviderResource firstResource = new ProxyProviderResource(
                "Auto-testing-reference-resource-2",
                "proxy/dir22/subdir1");
        ProxyProviderResource secondResource = new ProxyProviderResource(
                "Auto-testing-reference-resource-2",
                "proxy/dir22/subdir2");

        var response = new ServiceProvidersController()
                .withAdminToken()
                .addResourceListToProvider(awsS3Provider.getId(), firstResource);
        new ProxyProviderAssertion(response)
                .expectedCode(HttpStatus.SC_OK);

        var responseSecond = new ServiceProvidersController()
                .withAdminToken()
                .addResourceListToProvider(awsS3Provider.getId(), secondResource);
        new ProxyProviderAssertion(responseSecond)
                .expectedError(ProxyErrorList.getProviderResourceAlreadyExistsError(
                        "Auto-testing-reference-resource-2", "proxy/dir22/subdir1"))
                .expectedCode(HttpStatus.SC_CONFLICT);
    }
}
