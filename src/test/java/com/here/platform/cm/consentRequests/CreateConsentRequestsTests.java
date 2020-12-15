package com.here.platform.cm.consentRequests;

import com.here.platform.cm.BaseCMTest;
import com.here.platform.cm.rest.model.AdditionalLink;
import com.here.platform.cm.rest.model.ConsentRequest;
import com.here.platform.cm.rest.model.ConsentRequestData;
import com.here.platform.cm.rest.model.ConsentRequestIdResponse;
import com.here.platform.cm.rest.model.ConsentRequestStatus;
import com.here.platform.common.ResponseAssertion;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import com.here.platform.common.VinsToFile;
import com.here.platform.common.annotations.CMFeatures.CreateConsentRequest;
import com.here.platform.common.config.Conf;
import com.here.platform.common.extensions.ConsentRequestRemoveExtension;
import com.here.platform.common.extensions.OnboardAndRemoveApplicationExtension;
import com.here.platform.common.strings.VIN;
import com.here.platform.ns.dto.User;
import com.here.platform.ns.dto.Users;
import io.qameta.allure.Issue;
import java.io.File;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;


@DisplayName("Create consent request")
@CreateConsentRequest
@Tag("smoke_cm")
public class CreateConsentRequestsTests extends BaseCMTest {

    private final User mpConsumer = Users.MP_CONSUMER.getUser();
    private final ConsentRequestData testConsentRequest = new ConsentRequestData()
            .consumerId(mpConsumer.getRealm())
            .providerId(crypto.sha1())
            .title(Conf.cm().getQaTestDataMarker() + faker.gameOfThrones().quote())
            .purpose(faker.commerce().productName())
            .privacyPolicy(faker.internet().url())
            .addAdditionalLinksItem(
                    new AdditionalLink().title(faker.commerce().department()).url(faker.internet().url()))
            .containerId(crypto.sha1());

    @RegisterExtension
    OnboardAndRemoveApplicationExtension onboardApplicationExtension = OnboardAndRemoveApplicationExtension.builder()
            .consentRequestData(testConsentRequest).build();
    @RegisterExtension
    ConsentRequestRemoveExtension consentRequestRemoveExtension = new ConsentRequestRemoveExtension();

    @Test
    @DisplayName("Success flow of consent request creation and getting consent request by ID")
    @Tag("fabric_test")
    void createAndGetConsentRequest() {
        var crid = createConsentRequestWith(testConsentRequest);

        ConsentRequest expectedResponse = new ConsentRequest()
                .consentRequestId(crid)
                .additionalLinks(testConsentRequest.getAdditionalLinks())
                .privacyPolicy(testConsentRequest.getPrivacyPolicy())
                .consumerId(testConsentRequest.getConsumerId())
                .providerId(testConsentRequest.getProviderId())
                .containerId(testConsentRequest.getContainerId())
                .purpose(testConsentRequest.getPurpose())
                .title(testConsentRequest.getTitle());

        var consentRequestByIdResponse = consentRequestController
                .withConsumerToken()
                .getConsentRequestById(crid);
        new ResponseAssertion(consentRequestByIdResponse)
                .statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEqualToObject(expectedResponse);
    }

    @Test
    @DisplayName("Verify Adding Vins To Empty ConsentRequest Via File")
    @Tag("fabric_test")
    void addVinsToEmptyConsentRequestViaFileTest() {
        var crid = createConsentRequestWith(testConsentRequest);

        var testVin = VIN.generate(17);
        var testVin1 = VIN.generate(17);
        File testFileWithVINs = new VinsToFile(testVin, testVin1).json();
        consentRequestRemoveExtension.vinToRemove(testVin, testVin1);

        consentRequestController
                .withConsumerToken()
                .addVinsToConsentRequest(crid, testFileWithVINs);
        fuSleep();
        var expectedConsentRequestStatuses = new ConsentRequestStatus()
                .pending(2)
                .approved(0).revoked(0).expired(0).rejected(0);

        consentRequestController.withConsumerToken();
        var statusForConsentRequestByIdResponse = consentRequestController
                .getStatusForConsentRequestById(crid);
        new ResponseAssertion(statusForConsentRequestByIdResponse)
                .statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEqualToObject(expectedConsentRequestStatuses);

    }

    @Issue("NS-3043")
    @Test
    @DisplayName("Is possible to create consent reqeust without privacy policy and additional links")
    void isPossibleToCreateConsentReqeustWithoutPrivacyPolicyAndAdditionalLinks() {
        testConsentRequest.privacyPolicy(null).additionalLinks(null);
        var consentRequestResponse = consentRequestController
                .withConsumerToken()
                .createConsentRequest(testConsentRequest);

        var crid = new ResponseAssertion(consentRequestResponse)
                .statusCodeIsEqualTo(StatusCode.CREATED)
                .bindAs(ConsentRequestIdResponse.class).getConsentRequestId();
        consentRequestRemoveExtension.cridToRemove(crid);

        var expectedConsentRequestResponse = new ConsentRequest()
                .consentRequestId(crid)
                .additionalLinks(null)
                .privacyPolicy("/") //if privacy policy is empty - service should set default value to the field "/"
                .consumerId(testConsentRequest.getConsumerId())
                .containerId(testConsentRequest.getContainerId())
                .providerId(testConsentRequest.getProviderId())
                .title(testConsentRequest.getTitle())
                .purpose(testConsentRequest.getPurpose());

        var getConsentRequestData = consentRequestController.getConsentRequestById(crid);
        new ResponseAssertion(getConsentRequestData)
                .statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEqualToObject(expectedConsentRequestResponse);
    }

    private String createConsentRequestWith(ConsentRequestData targetConsentRequest) {
        consentRequestController.withConsumerToken();
        final var actualResponse = consentRequestController.createConsentRequest(testConsentRequest);
        var crid = new ResponseAssertion(actualResponse)
                .statusCodeIsEqualTo(StatusCode.CREATED)
                .bindAs(ConsentRequestIdResponse.class)
                .getConsentRequestId();
        consentRequestRemoveExtension.cridToRemove(crid);
        return crid;
    }

}
