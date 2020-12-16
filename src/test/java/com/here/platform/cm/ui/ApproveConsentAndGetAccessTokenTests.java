package com.here.platform.cm.ui;

import static com.codeborne.selenide.Selenide.open;

import com.here.platform.cm.controllers.AccessTokenController;
import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.enums.ProviderApplications;
import com.here.platform.cm.pages.LandingPage;
import com.here.platform.cm.pages.VINEnteringPage;
import com.here.platform.cm.rest.model.AccessTokenResponse;
import com.here.platform.cm.rest.model.ConsentInfo;
import com.here.platform.cm.steps.api.ConsentRequestSteps;
import com.here.platform.cm.steps.api.UserAccountSteps;
import com.here.platform.cm.steps.ui.OfferDetailsPageSteps;
import com.here.platform.cm.steps.ui.SuccessConsentPageSteps;
import com.here.platform.common.DataSubject;
import com.here.platform.common.ResponseAssertion;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import com.here.platform.common.config.Conf;
import com.here.platform.common.strings.VIN;
import com.here.platform.dataProviders.daimler.DataSubjects;
import com.here.platform.dataProviders.daimler.steps.DaimlerLoginPage;
import com.here.platform.dataProviders.reference.steps.ReferenceApprovePage;
import com.here.platform.hereAccount.controllers.HereUserManagerController.HereUser;
import com.here.platform.hereAccount.ui.HereLoginSteps;
import com.here.platform.ns.dto.User;
import com.here.platform.ns.helpers.authentication.AuthController;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


@DisplayName("Approve and get access token E2E")
class ApproveConsentAndGetAccessTokenTests extends BaseUITests {

    private final User mpConsumer = providerApplication.consumer;
    private final List<String> cridsToRemove = new ArrayList<>();
    HereUser hereUser = null;
    DataSubject dataSubjectIm;
    private String crid;

    //todo refactor as extension https://www.baeldung.com/junit-5-extensions
    @BeforeEach
    void beforeEach() {
        hereUser = new HereUser(faker.internet().emailAddress(), faker.internet().password(), "here");
        dataSubjectIm = new DataSubject(
                hereUser.getEmail(),
                hereUser.getPassword(),
                VIN.generate(providerApplication.provider.vinLength)
        );
        hereUserManagerController.createHereUser(hereUser);
    }

    @AfterEach
    void afterEach() {
        var privateBearer = AuthController.getDataSubjectToken(dataSubjectIm);
        userAccountController.deleteVINForUser(dataSubjectIm.getVin(), privateBearer);
        AuthController.deleteToken(dataSubjectIm);
        if (hereUser != null) {
            hereUserManagerController.deleteHereUser(hereUser);
        }
    }


    @Test
    @DisplayName("E2E success flow to approve consent request and get access token for one. Reference(ISO) provider")
    @Tag("dynamic_ui")
    void e2eTest() {
        var vin = dataSubjectIm.getVin();
        ConsentInfo consentRequest = ConsentRequestSteps
                .createValidConsentRequestWithNSOnboardings(providerApplication, vin, testContainer);
        crid = consentRequest.getConsentRequestId();

        open(crid);
        new LandingPage().isLoaded().clickSignIn();
        HereLoginSteps.loginNewDataSubjectWithHEREConsentApprove(dataSubjectIm);
        new VINEnteringPage().isLoaded().fillVINAndContinue(vin);
        cridsToRemove.add(crid);

        OfferDetailsPageSteps.verifyConsentDetailsPageAndCountinue(consentRequest);

        ReferenceApprovePage.approveReferenceScopesAndSubmit(vin);

        SuccessConsentPageSteps.verifyFinalPage(consentRequest);

        var accessTokenController = new AccessTokenController();
        accessTokenController.withConsumerToken();
        var accessTokenResponse = accessTokenController.getAccessToken(crid, vin, mpConsumer.getRealm());

        new ResponseAssertion(accessTokenResponse)
                .statusCodeIsEqualTo(StatusCode.OK)
                .bindAs(AccessTokenResponse.class);
    }

    @Test
    @DisplayName("E2E success flow to approve consent request and get access token for one. Daimler(ISO) experimental provider")
    @Tag("dynamic_ui")
    void e2eTestDaimler() {
        var targetDaimlerDataSubject = DataSubjects.getNextBy18VINLength();
        dataSubjectIm = targetDaimlerDataSubject.dataSubject;
        UserAccountSteps.removeVINFromDataSubject(targetDaimlerDataSubject);

        providerApplication = ProviderApplications.DAIMLER_CONS_1;
        testContainer = ConsentRequestContainers.generateNew(providerApplication.provider);
        testContainer.setClientId(Conf.cmUsers().getDaimlerApp().getClientId());
        testContainer.setClientSecret(Conf.cmUsers().getDaimlerApp().getClientSecret());
        var vin = dataSubjectIm.getVin();
        ConsentInfo consentRequest = ConsentRequestSteps
                .createValidConsentRequestWithNSOnboardings(providerApplication, vin, testContainer);
        crid = consentRequest.getConsentRequestId();

        open(crid);
        new LandingPage().isLoaded().clickSignIn();
        HereLoginSteps.loginRegisteredDataSubject(dataSubjectIm);
        new VINEnteringPage().isLoaded().fillVINAndContinue(vin);
        cridsToRemove.add(vin);

        OfferDetailsPageSteps.verifyConsentDetailsPageAndCountinue(consentRequest);

        DaimlerLoginPage.loginDataSubjectOnDaimlerSite(targetDaimlerDataSubject.dataSubject);
        if (!SuccessConsentPageSteps.isLoaded()) {
            DaimlerLoginPage.approveDaimlerLegalAndSubmit();
            DaimlerLoginPage.approveDaimlerScopesAndSubmit();
        }

        SuccessConsentPageSteps.verifyFinalPage(consentRequest);

        var accessTokenController = new AccessTokenController();
        accessTokenController.withConsumerToken();
        var accessTokenResponse = accessTokenController.getAccessToken(crid, vin, mpConsumer.getRealm());

        new ResponseAssertion(accessTokenResponse)
                .statusCodeIsEqualTo(StatusCode.OK)
                .bindAs(AccessTokenResponse.class);
    }

    //todo automate test when the user has 1 consent request for his 2 cars

    //todo automate following scenario, user has a car and consent request,
    // but received a new consent request for another his car, onpen the second consent request and add new car
    // to approve new one
}
