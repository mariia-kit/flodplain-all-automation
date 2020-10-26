package com.here.platform.cm.ui;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.Selenide.sleep;
import static com.here.platform.cm.rest.model.ConsentInfo.StateEnum.APPROVED;
import static com.here.platform.cm.rest.model.ConsentInfo.StateEnum.PENDING;
import static com.here.platform.cm.rest.model.ConsentInfo.StateEnum.REVOKED;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.SelenideElement;
import com.here.platform.cm.controllers.HERETokenController;
import com.here.platform.cm.enums.ConsentPageUrl;
import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.enums.MPConsumers;
import com.here.platform.cm.enums.ProviderApplications;
import com.here.platform.cm.pages.VINEnteringPage;
import com.here.platform.cm.rest.model.ConsentInfo;
import com.here.platform.cm.steps.api.ConsentFlowSteps;
import com.here.platform.cm.steps.api.ConsentRequestSteps;
import com.here.platform.cm.steps.api.RemoveEntitiesSteps;
import com.here.platform.cm.steps.ui.OfferDetailsPageSteps;
import com.here.platform.common.DataSubject;
import com.here.platform.common.VIN;
import com.here.platform.common.VinsToFile;
import com.here.platform.dataProviders.daimler.DataSubjects;
import com.here.platform.hereAccount.controllers.HereUserManagerController;
import com.here.platform.hereAccount.controllers.HereUserManagerController.HereUser;
import com.here.platform.hereAccount.ui.HereLoginSteps;
import io.qameta.allure.Step;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


@DisplayName("Verify Dashboard UI")
@Tag("ui")
public class DashboardTests extends BaseUITests {

    private final List<String> cridsToRemove = new ArrayList<>();
    private final ProviderApplications providerApplication = ProviderApplications.REFERENCE_CONS_1;
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

    //todo add test for offers opening

    //todo add tests to approve and revoke consents from dashboard

    //todo add test for new  Data Subject registration on HERE after open consent request URL and delete created account

    @Test
    @DisplayName("Verify Dashboard page")
    void verifyDashBoardTest() {
        var mpConsumer = providerApplication.consumer;
        var vin = dataSubjectIm.getVin();
        ConsentRequestContainers testContainer1 = ConsentRequestContainers
                .generateNew(providerApplication.provider.getName());
        var firstConsentRequest = ConsentRequestSteps.createValidConsentRequestWithNSOnboardings(providerApplication, vin, testContainer1);
        var consentRequestId1 = firstConsentRequest.getConsentRequestId();
        cridsToRemove.add(consentRequestId1);
        ConsentRequestContainers testContainer2 = ConsentRequestContainers
                .generateNew(providerApplication.provider.getName());
        var secondConsentRequest = ConsentRequestSteps.createValidConsentRequestWithNSOnboardings(providerApplication, vin, testContainer2);
        var consentRequestId2 = secondConsentRequest.getConsentRequestId();
        cridsToRemove.add(consentRequestId2);

        open(ConsentPageUrl.getEnvUrlRoot());
        HereLoginSteps.loginDataSubject(dataSubjectIm);
        new VINEnteringPage().isLoaded().fillVINAndContinue(vin);
        String token = getUICmToken();

        $(".offers-list").waitUntil(Condition.visible, 10000);
        openDashboardNewTab();
        verifyConsentOfferTab(0, mpConsumer, secondConsentRequest, vin, PENDING);
        verifyConsentOfferTab(1, mpConsumer, firstConsentRequest, vin, PENDING);

        ConsentFlowSteps.approveConsentForVIN(consentRequestId1, testContainer, vin, token);
        ConsentFlowSteps.approveConsentForVIN(consentRequestId2, testContainer, vin, token);

        openDashboardAcceptedTab();
        verifyConsentOfferTab(0, mpConsumer, secondConsentRequest, vin, APPROVED);
        verifyConsentOfferTab(1, mpConsumer, firstConsentRequest, vin, APPROVED);

        ConsentFlowSteps.revokeConsentForVIN(consentRequestId1, vin, token);
        ConsentFlowSteps.revokeConsentForVIN(consentRequestId2, vin, token);

        openDashboardRevokedTab();
        verifyConsentOfferTab(0, mpConsumer, secondConsentRequest, vin, REVOKED);
        verifyConsentOfferTab(1, mpConsumer, firstConsentRequest, vin, REVOKED);
    }

    @Test
    @DisplayName("Verify Open Dashboard page")
    @Tag("dynamic_ui")
    void verifyOpenDashBoardTest() {
        var vin = dataSubjectIm.getVin();
        ConsentRequestContainers testContainer1 = ConsentRequestContainers
                .generateNew(providerApplication.provider.getName());
        var firstConsentRequest = ConsentRequestSteps
                .createValidConsentRequestWithNSOnboardings(providerApplication, vin, testContainer1);
        var crid = firstConsentRequest.getConsentRequestId();
        cridsToRemove.add(crid);

        open(Configuration.baseUrl + crid);

        HereLoginSteps.loginDataSubject(dataSubjectIm);

        new VINEnteringPage().isLoaded().fillVINAndContinue(vin);

        OfferDetailsPageSteps.viewAllOffers();
        $(".offers-list").waitUntil(Condition.visible, 10000);
    }

    @Step
    private void openDashboardNewTab() {
        sleep(3000); //hotfix cos of FE developer rotation
        $("lui-tab[data-cy='new']").click();
    }

    @Step
    private void openDashboardRevokedTab() {
        sleep(3000); //hotfix cos of FE developer rotation
        $("lui-tab[data-cy='revoked']").click();
        $("lui-tab[data-cy='revoked']").click(); //hotfix cos of FE developer rotation
    }

    @Step
    private void openDashboardAcceptedTab() {
        sleep(3000); //hotfix cos of FE developer rotation
        $("lui-tab[data-cy='accepted']").click();
    }

    @Step
    private void verifyConsentOfferTab(int index,
            MPConsumers mpConsumer, ConsentInfo consentRequest,
            String vinNumber, ConsentInfo.StateEnum status
    ) {
        //TODO reuse consent request id to find offer box after 04.05
        SelenideElement offerBox = $("div.offer-box", index).shouldBe(Condition.visible);
        offerBox.$(".offer-title").shouldHave(Condition.text(consentRequest.getTitle()));
        offerBox.$(".provider-name").shouldHave(Condition.text(mpConsumer.getConsumerName()));
        offerBox.$(".offer-description").shouldHave(Condition.text(consentRequest.getPurpose()));
        offerBox.$(".vin-code").shouldHave(Condition.text(new VIN(vinNumber).label()));
        if (!PENDING.equals(status)) {
            offerBox.$("lui-status").shouldHave(Condition.text(status.name()));
        } else {
            offerBox.$("lui-status").shouldNotBe(Condition.visible);
        }
    }

}
