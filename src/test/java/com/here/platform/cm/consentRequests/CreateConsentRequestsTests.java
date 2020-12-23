package com.here.platform.cm.consentRequests;

import com.here.platform.cm.BaseCMTest;
import com.here.platform.cm.enums.ConsentRequestContainer;
import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.enums.Consents;
import com.here.platform.cm.enums.MPProviders;
import com.here.platform.cm.enums.ProviderApplications;
import com.here.platform.cm.rest.model.ConsentInfo;
import com.here.platform.cm.rest.model.ConsentRequest;
import com.here.platform.cm.rest.model.ConsentRequestStatus;
import com.here.platform.cm.steps.api.ConsentRequestSteps2;
import com.here.platform.common.ResponseAssertion;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import com.here.platform.common.VinsToFile.FILE_TYPE;
import com.here.platform.common.annotations.CMFeatures.CreateConsentRequest;
import com.here.platform.common.strings.VIN;
import com.here.platform.ns.dto.User;
import com.here.platform.ns.dto.Users;
import io.qameta.allure.Issue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


@DisplayName("Create consent request")
@CreateConsentRequest
@Tag("smoke_cm")
public class CreateConsentRequestsTests extends BaseCMTest {

    private final User mpConsumer = Users.MP_CONSUMER.getUser();

    @Test
    @DisplayName("Success flow of consent request creation and getting consent request by ID")
    @Tag("fabric_test")
    void createAndGetConsentRequest() {
        ProviderApplications targetApp = ProviderApplications.REFERENCE_CONS_1;
        User mpConsumer = Users.MP_CONSUMER.getUser();
        ConsentRequestContainer targetContainer = ConsentRequestContainers.generateNew(targetApp.getProvider());

        ConsentInfo consentInfo = Consents.generateNewConsentInfo(mpConsumer, targetContainer);
        var crid = new ConsentRequestSteps2(targetApp.getProvider().getName(), consentInfo)
                .onboardAllForConsentRequest()
                .createConsentRequest()
                .getId();

        ConsentRequest expectedResponse = Consents.generateResponse(targetApp.getProvider().getName(), consentInfo);

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
        ProviderApplications targetApp = ProviderApplications.REFERENCE_CONS_1;
        User mpConsumer = Users.MP_CONSUMER.getUser();
        ConsentRequestContainer targetContainer = ConsentRequestContainers.generateNew(targetApp.getProvider());
        var testVin = VIN.generate(17);
        var testVin1 = VIN.generate(17);

        ConsentInfo consentInfo = Consents.generateNewConsentInfo(mpConsumer, targetContainer);
        var crid = new ConsentRequestSteps2(targetApp.getProvider().getName(), consentInfo)
                .onboardAllForConsentRequest()
                .createConsentRequest()
                .getId();

        consentRequestController
                .withConsumerToken()
                .addVinsToConsentRequest(crid, FILE_TYPE.JSON, testVin, testVin1);

        var expectedConsentRequestStatuses = new ConsentRequestStatus()
                .pending(2)
                .approved(0).revoked(0).expired(0).rejected(0);

        var statusForConsentRequestByIdResponse = consentRequestController
                .withConsumerToken()
                .getStatusForConsentRequestById(crid);
        new ResponseAssertion(statusForConsentRequestByIdResponse)
                .statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEqualToObject(expectedConsentRequestStatuses);

    }

    @Issue("NS-3043")
    @Test
    @DisplayName("Is possible to create consent request without privacy policy and additional links")
    void isPossibleToCreateConsentReqeustWithoutPrivacyPolicyAndAdditionalLinks() {
        ProviderApplications targetApp = ProviderApplications.REFERENCE_CONS_1;
        User mpConsumer = Users.MP_CONSUMER.getUser();
        ConsentRequestContainer targetContainer = ConsentRequestContainers.generateNew(targetApp.getProvider());

        ConsentInfo consentInfo = Consents
                .generateNewConsentInfo(mpConsumer, targetContainer)
                .privacyPolicy(null)
                .additionalLinks(null);
        var crid = new ConsentRequestSteps2(targetApp.getProvider().getName(), consentInfo)
                .onboardAllForConsentRequest()
                .createConsentRequest()
                .getId();

        var expectedConsentRequestResponse = Consents
                .generateResponse(targetApp.getProvider().getName(), consentInfo);

        var getConsentRequestData = consentRequestController
                .withConsumerToken()
                .getConsentRequestById(crid);
        new ResponseAssertion(getConsentRequestData)
                .statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEqualToObject(expectedConsentRequestResponse);
    }

}
