package com.here.platform.cm.ui;

import static com.codeborne.selenide.Selenide.open;

import com.here.platform.cm.controllers.AccessTokenController;
import com.here.platform.cm.enums.ConsentObject;
import com.here.platform.cm.enums.ConsentRequestContainer;
import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.enums.MPProviders;
import com.here.platform.cm.pages.LandingPage;
import com.here.platform.cm.pages.VINEnteringPage;
import com.here.platform.cm.rest.model.AccessTokenResponse;
import com.here.platform.cm.steps.api.ConsentRequestSteps;
import com.here.platform.cm.steps.api.UserAccountSteps;
import com.here.platform.cm.steps.ui.OfferDetailsPageSteps;
import com.here.platform.cm.steps.ui.SuccessConsentPageSteps;
import com.here.platform.common.DataSubject;
import com.here.platform.common.ResponseAssertion;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import com.here.platform.dataProviders.daimler.DataSubjects;
import com.here.platform.dataProviders.daimler.steps.DaimlerLoginPage;
import com.here.platform.dataProviders.reference.steps.ReferenceApprovePage;
import com.here.platform.hereAccount.ui.HereLoginSteps;
import com.here.platform.ns.dto.User;
import com.here.platform.ns.dto.Users;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Disabled
@DisplayName("[UI] Approve and get access token E2E")
class ApproveConsentAndGetAccessTokenTests extends BaseUITests {

    @Test
    @DisplayName("E2E success flow to approve consent request and get access token for one. Reference(ISO) provider")
    @Tag("dynamic_ui")
    void e2eTest() {
        MPProviders provider = MPProviders.REFERENCE;
        User mpConsumer = Users.MP_CONSUMER.getUser();
        ConsentRequestContainer targetContainer = ConsentRequestContainers.generateNew(provider);
        ConsentObject consentObj = new ConsentObject(mpConsumer, provider, targetContainer);
        DataSubject dataSubjectIm = UserAccountSteps.generateNewHereAccount(provider.getVinLength());

        var crid = new ConsentRequestSteps(consentObj)
                .onboardAllForConsentRequest()
                .createConsentRequest()
                .addVINsToConsentRequest(dataSubjectIm.getVin())
                .getId();

        open(crid);
        new LandingPage().isLoaded().clickSignIn();
        HereLoginSteps.loginNewDataSubjectWithHEREConsentApprove(dataSubjectIm);
        new VINEnteringPage().isLoaded().fillVINAndContinue(dataSubjectIm.getVin());

        OfferDetailsPageSteps.verifyConsentDetailsPageAndCountinue(consentObj.getConsent());

        ReferenceApprovePage.approveReferenceScopesAndSubmit(dataSubjectIm.getVin());

        SuccessConsentPageSteps.verifyFinalPage(consentObj.getConsent());

        var accessTokenController = new AccessTokenController();
        accessTokenController.withConsumerToken();
        var accessTokenResponse = accessTokenController.getAccessToken(crid, dataSubjectIm.getVin(), mpConsumer.getRealm());

        new ResponseAssertion(accessTokenResponse)
                .statusCodeIsEqualTo(StatusCode.OK)
                .bindAs(AccessTokenResponse.class);
    }

    @Test
    @DisplayName("E2E success flow to approve consent request and get access token for one. Daimler(ISO) experimental provider")
    void e2eTestDaimler() {
        MPProviders provider = MPProviders.DAIMLER_EXPERIMENTAL;
        User mpConsumer = Users.MP_CONSUMER.getUser();
        ConsentRequestContainer targetContainer = ConsentRequestContainers.generateNew(provider);
        ConsentObject consentObj = new ConsentObject(mpConsumer, provider, targetContainer);
        DataSubject dataSubjectIm = DataSubjects.getNextBy18VINLength().getDataSubject();
        UserAccountSteps.removeVINFromDataSubject(dataSubjectIm);

        var crid = new ConsentRequestSteps(consentObj)
                .onboardAllForConsentRequest()
                .createConsentRequest()
                .addVINsToConsentRequest(dataSubjectIm.getVin())
                .getId();

        open(crid);
        new LandingPage().isLoaded().clickSignIn();
        HereLoginSteps.loginRegisteredDataSubject(dataSubjectIm);
        new VINEnteringPage().isLoaded().fillVINAndContinue(dataSubjectIm.getVin());

        OfferDetailsPageSteps.verifyConsentDetailsPageAndCountinue(consentObj.getConsent());

        DaimlerLoginPage.loginDataSubjectOnDaimlerSite(dataSubjectIm);
        if (!SuccessConsentPageSteps.isLoaded()) {
            DaimlerLoginPage.approveDaimlerLegalAndSubmit();
            DaimlerLoginPage.approveDaimlerScopesAndSubmit();
        }

        SuccessConsentPageSteps.verifyFinalPage(consentObj.getConsent());

        var accessTokenController = new AccessTokenController();
        accessTokenController.withConsumerToken();
        var accessTokenResponse = accessTokenController
                .getAccessToken(crid, dataSubjectIm.getVin(), mpConsumer.getRealm());

        new ResponseAssertion(accessTokenResponse)
                .statusCodeIsEqualTo(StatusCode.OK)
                .bindAs(AccessTokenResponse.class);
    }


    //todo automate test when the user has 1 consent request for his 2 cars

    //todo automate following scenario, user has a car and consent request,
    // but received a new consent request for another his car, onpen the second consent request and add new car
    // to approve new one
}
