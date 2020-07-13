package com.here.platform.ns.provider.resources;

import static com.here.platform.ns.dto.Users.PROVIDER;

import com.here.platform.ns.BaseNSTest;
import com.here.platform.ns.dto.DataProvider;
import com.here.platform.ns.dto.ProviderResource;
import com.here.platform.ns.dto.Providers;
import com.here.platform.ns.dto.SentryErrorsList;
import com.here.platform.ns.dto.Users;
import com.here.platform.ns.helpers.NSErrors;
import com.here.platform.ns.restEndPoints.provider.data_providers.AddDataProviderCall;
import com.here.platform.ns.restEndPoints.provider.resources.AddProviderResourceCall;
import com.here.platform.ns.restEndPoints.provider.resources.DeleteProviderResourceCall;
import com.here.platform.ns.restEndPoints.provider.resources.GetProviderResourceCall;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


@DisplayName("Verify receive Resource for Data Provider data")
public class GetResourceTest extends BaseNSTest {

    @Test
    @DisplayName("Verify receive Resource data Successful")
    void verifyGetContainersDataRetrieved() {
        DataProvider provider = Providers.generateNew();
        new AddDataProviderCall(provider)
                .withToken(PROVIDER)
                .call()
                .expectedCode(HttpStatus.SC_OK);

        ProviderResource res = ProviderResource.generateNew();
        new AddProviderResourceCall(provider, res.getName())
                .call()
                .expectedCode(HttpStatus.SC_OK);

        new GetProviderResourceCall(provider, res.getName())
                .call()
                .expectedCode(HttpStatus.SC_OK)
                .expectedEquals("name", res.getName(), "Provider resource not as expected!");
    }

    @Test
    @DisplayName("Verify receive Resource data with no Token")
    void verifyGetContainersNoToken() {
        DataProvider provider = Providers.generateNew();
        new AddDataProviderCall(provider)
                .withToken(PROVIDER)
                .call()
                .expectedCode(HttpStatus.SC_OK);

        ProviderResource res = ProviderResource.generateNew();
        new AddProviderResourceCall(provider, res.getName())
                .call()
                .expectedCode(HttpStatus.SC_OK);

        new GetProviderResourceCall(provider, res.getName())
                .withToken(StringUtils.EMPTY)
                .call()
                .expectedSentryError(SentryErrorsList.TOKEN_NOT_FOUND.getError());
    }

    @Test
    @DisplayName("Verify receive Resource data with invalid Token")
    void verifyGetContainersInvalidToken() {
        DataProvider provider = Providers.generateNew();
        new AddDataProviderCall(provider)
                .withToken(PROVIDER)
                .call()
                .expectedCode(HttpStatus.SC_OK);

        ProviderResource res = ProviderResource.generateNew();
        new AddProviderResourceCall(provider, res.getName())
                .call()
                .expectedCode(HttpStatus.SC_OK);

        new GetProviderResourceCall(provider, res.getName())
                .withToken(Users.EXTERNAL_USER)
                .call()
                .expectedSentryError(SentryErrorsList.TOKEN_INVALID.getError());
    }

    @Test
    @DisplayName("Verify receive Resource data already Deleted")
    void verifyGetContainersDataAlreadyDeleted() {
        DataProvider provider = Providers.generateNew();
        new AddDataProviderCall(provider)
                .withToken(PROVIDER)
                .call()
                .expectedCode(HttpStatus.SC_OK);

        ProviderResource res = ProviderResource.generateNew();
        new AddProviderResourceCall(provider, res.getName())
                .call()
                .expectedCode(HttpStatus.SC_OK);

        new DeleteProviderResourceCall(provider, res.getName())
                .call()
                .expectedCode(HttpStatus.SC_NO_CONTENT);

        new GetProviderResourceCall(provider, res.getName())
                .call()
                .expectedError(NSErrors.getProviderResourceNotFoundError(provider.getName(),
                        res.getName()));
    }

    @Test
    @DisplayName("Verify receive Resource data with not valid Provider")
    void verifyGetContainersDataNoProvider() {
        DataProvider provider = Providers.generateNew();
        new AddDataProviderCall(provider)
                .withToken(PROVIDER)
                .call()
                .expectedCode(HttpStatus.SC_OK);

        ProviderResource res = ProviderResource.generateNew();
        new AddProviderResourceCall(provider, res.getName())
                .call()
                .expectedCode(HttpStatus.SC_OK);
        provider.setName("no_such_provider");
        new GetProviderResourceCall(provider, res.getName())
                .call()
                .expectedError(NSErrors.getProviderNotFoundError(provider));
    }

    @Test
    @DisplayName("Verify receive Resource data with not valid resource name")
    void verifyGetContainersDataNoContainer() {
        DataProvider provider = Providers.generateNew();
        new AddDataProviderCall(provider)
                .withToken(PROVIDER)
                .call()
                .expectedCode(HttpStatus.SC_OK);

        ProviderResource res = ProviderResource.generateNew();
        new AddProviderResourceCall(provider, res.getName())
                .call()
                .expectedCode(HttpStatus.SC_OK);

        new GetProviderResourceCall(provider, "no_such_res")
                .call()
                .expectedError(NSErrors.getProviderResourceNotFoundError(provider.getName(),
                        "no_such_res"));
    }
}
