package com.here.platform.cm.ui;

import static com.codeborne.selenide.Selenide.open;
import static com.here.platform.cm.rest.model.ConsentInfo.StateEnum.APPROVED;
import static com.here.platform.cm.rest.model.ConsentInfo.StateEnum.PENDING;
import static com.here.platform.cm.rest.model.ConsentInfo.StateEnum.REVOKED;

import com.here.platform.cm.controllers.HERETokenController;
import com.here.platform.cm.enums.ConsentPageUrl;
import com.here.platform.cm.enums.ConsentRequestContainer;
import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.pages.DashBoardPage;
import com.here.platform.cm.pages.VINEnteringPage;
import com.here.platform.cm.steps.api.ConsentFlowSteps;
import com.here.platform.cm.steps.api.ConsentRequestSteps;
import com.here.platform.cm.steps.api.RemoveEntitiesSteps;
import com.here.platform.cm.steps.ui.OfferDetailsPageSteps;
import com.here.platform.cm.steps.ui.SuccessConsentPageSteps;
import com.here.platform.common.DataSubject;
import com.here.platform.common.VinsToFile;
import com.here.platform.common.strings.VIN;
import com.here.platform.dataProviders.reference.steps.ReferenceApprovePage;
import com.here.platform.hereAccount.controllers.HereUserManagerController;
import com.here.platform.hereAccount.controllers.HereUserManagerController.HereUser;
import com.here.platform.hereAccount.ui.HereLoginSteps;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


@DisplayName("Verify Dashboard UI")
@Tag("dynamic_ui")
public class DashboardTests extends BaseUITests {

    private final List<String> cridsToRemove = new ArrayList<>();
    HereUser hereUser = null;
    DataSubject dataSubjectIm;

    @BeforeEach
    void beforeEach() {
        hereUser = new HereUser(faker.internet().emailAddress(), faker.internet().password(), "here");
        dataSubjectIm = new DataSubject();
        dataSubjectIm.setEmail(hereUser.getEmail());
        dataSubjectIm.setPass(hereUser.getPassword());
        dataSubjectIm.setVin(VIN.generate(providerApplication.provider.vinLength));
        new HereUserManagerController().createHereUser(hereUser);
    }

    @AfterEach
    void afterEach() {
        var privateBearer =  new HERETokenController().loginAndGenerateCMToken(dataSubjectIm.getEmail(), dataSubjectIm.getPass());
        userAccountController.deleteVINForUser(dataSubjectIm.getVin(), privateBearer);
        for (String crid : cridsToRemove) {
            RemoveEntitiesSteps.forceRemoveConsentRequestWithConsents(
                    crid,
                    new VinsToFile(dataSubjectIm.getVin()).json()
            );
        }
        if (hereUser != null) {
            new HereUserManagerController().deleteHereUser(hereUser);
        }
    }

    //todo add tests to approve and revoke consents from dashboard

    @Test
    @DisplayName("Verify Dashboard page")
    void verifyDashBoardTest() {
        var mpConsumer = providerApplication.consumer;
        var vin = dataSubjectIm.getVin();
        ConsentRequestContainer testContainer1 = ConsentRequestContainers
                .generateNew(providerApplication.provider);
        var firstConsentRequest = ConsentRequestSteps.createValidConsentRequestWithNSOnboardings(providerApplication, vin, testContainer1);
        var consentRequestId1 = firstConsentRequest.getConsentRequestId();
        cridsToRemove.add(consentRequestId1);
        ConsentRequestContainer testContainer2 = ConsentRequestContainers
                .generateNew(providerApplication.provider);
        var secondConsentRequest = ConsentRequestSteps.createValidConsentRequestWithNSOnboardings(providerApplication, vin, testContainer2);
        var consentRequestId2 = secondConsentRequest.getConsentRequestId();
        cridsToRemove.add(consentRequestId2);

        open(ConsentPageUrl.getEnvUrlRoot());
        HereLoginSteps.loginNewDataSubjectWithHEREConsentApprove(dataSubjectIm);
        new VINEnteringPage().isLoaded().fillVINAndContinue(vin);
        String token = getUICmToken();

        new DashBoardPage()
                .isLoaded()
                .openDashboardNewTab()
                .verifyConsentOfferTab(0, mpConsumer, secondConsentRequest, vin, PENDING)
                .verifyConsentOfferTab(1, mpConsumer, firstConsentRequest, vin, PENDING);

        ConsentFlowSteps.approveConsentForVIN(consentRequestId1, testContainer, vin, token);
        ConsentFlowSteps.approveConsentForVIN(consentRequestId2, testContainer, vin, token);

        new DashBoardPage()
                .isLoaded()
                .openDashboardAcceptedTab()
                .verifyConsentOfferTab(0, mpConsumer, secondConsentRequest, vin, APPROVED)
                .verifyConsentOfferTab(1, mpConsumer, firstConsentRequest, vin, APPROVED);

        ConsentFlowSteps.revokeConsentForVIN(consentRequestId1, vin, token);
        ConsentFlowSteps.revokeConsentForVIN(consentRequestId2, vin, token);

        new DashBoardPage()
                .isLoaded()
                .openDashboardRevokedTab()
                .verifyConsentOfferTab(0, mpConsumer, secondConsentRequest, vin, REVOKED)
                .verifyConsentOfferTab(1, mpConsumer, firstConsentRequest, vin, REVOKED);
    }

    @Test
    @DisplayName("Verify Open Dashboard page")
    void verifyOpenDashBoardTest() {
        var vin = dataSubjectIm.getVin();
        ConsentRequestContainer testContainer1 = ConsentRequestContainers
                .generateNew(providerApplication.provider);
        var firstConsentRequest = ConsentRequestSteps
                .createValidConsentRequestWithNSOnboardings(providerApplication, vin, testContainer1);
        var crid = firstConsentRequest.getConsentRequestId();
        cridsToRemove.add(crid);

        open(crid);

        HereLoginSteps.loginNewDataSubjectWithHEREConsentApprove(dataSubjectIm);

        new VINEnteringPage().isLoaded().fillVINAndContinue(vin);

        OfferDetailsPageSteps.closeCurrentOffer();
        new DashBoardPage().isLoaded();
    }

    @Test
    @DisplayName("Verify Revoke thru Dashboard page")
    void verifyRevokeDashboardTest() {
        var vin = dataSubjectIm.getVin();
        ConsentRequestContainer testContainer1 = ConsentRequestContainers
                .generateNew(providerApplication.provider);
        var consentRequest = ConsentRequestSteps
                .createValidConsentRequestWithNSOnboardings(providerApplication, vin, testContainer1);
        var crid = consentRequest.getConsentRequestId();
        cridsToRemove.add(crid);

        open(crid);

        HereLoginSteps.loginNewDataSubjectWithHEREConsentApprove(dataSubjectIm);

        new VINEnteringPage().isLoaded().fillVINAndContinue(vin);

        OfferDetailsPageSteps.verifyConsentDetailsPageAndCountinue(consentRequest);
        ReferenceApprovePage.approveReferenceScopesAndSubmit(vin);
        SuccessConsentPageSteps.verifyFinalPage(consentRequest);
        SuccessConsentPageSteps.openAllOffersLink();

        new DashBoardPage()
                .isLoaded()
                .openDashboardAcceptedTab()
                .openConsentRequestOfferBox(consentRequest);
        OfferDetailsPageSteps.verifyConsentDetailsPage(consentRequest);
        OfferDetailsPageSteps.revokeConsent();
        OfferDetailsPageSteps.revokeConsentPopupYes();

        new DashBoardPage()
                .isLoaded()
                .openDashboardRevokedTab()
                .verifyConsentOfferTab(0, providerApplication.consumer, consentRequest, vin, REVOKED)
                .openConsentRequestOfferBox(consentRequest);
        OfferDetailsPageSteps.verifyConsentDetailsPage(consentRequest);
    }

}
