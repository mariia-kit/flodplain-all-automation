package com.here.platform.cm.ui;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static com.here.platform.cm.rest.model.ConsentInfo.StateEnum.APPROVED;
import static com.here.platform.cm.rest.model.ConsentInfo.StateEnum.PENDING;
import static com.here.platform.cm.rest.model.ConsentInfo.StateEnum.REVOKED;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.here.platform.cm.enums.ConsentPageUrl;
import com.here.platform.cm.enums.MPConsumers;
import com.here.platform.cm.enums.ProviderApplications;
import com.here.platform.cm.rest.model.ConsentInfo;
import com.here.platform.cm.steps.api.ConsentFlowSteps;
import com.here.platform.cm.steps.api.ConsentRequestSteps;
import com.here.platform.cm.steps.api.RemoveEntitiesSteps;
import com.here.platform.common.VIN;
import com.here.platform.common.VinsToFile;
import com.here.platform.hereAccount.ui.HereLoginSteps;
import io.qameta.allure.Step;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


@DisplayName("Verify Dashboard UI")
@Tag("ui")
public class DashboardTests extends BaseUITests {

    private final List<String> cridsToRemove = new ArrayList<>();
    private final ProviderApplications providerApplication = ProviderApplications.DAIMLER_CONS_1;

    @BeforeEach
    void beforeEach() {
        var privateBearer = dataSubject.getBearerToken();
        userAccountController.deleteConsumerForUser(providerApplication.consumer.getRealm(), privateBearer);
        userAccountController.deleteVINForUser(dataSubject.getVin(), privateBearer);
    }

    @AfterEach
    void afterEach() {
        userAccountController.deleteVINForUser(dataSubject.getVin(), dataSubject.getBearerToken());
        userAccountController
                .deleteConsumerForUser(providerApplication.consumer.getRealm(), dataSubject.getBearerToken());
        for (String crid : cridsToRemove) {
            RemoveEntitiesSteps.forceRemoveConsentRequestWithConsents(crid, new VinsToFile(dataSubject.getVin()).json());
        }
    }

    //todo add test for offers opening

    //todo add tests to approve and revoke consents from dashboard

    //todo add test for new  Data Subject registration on HERE after open consent request URL and delete created account

    @Test
    @DisplayName("Verify Dashboard page")
    void verifyDashBoardTest() {
        var mpConsumer = providerApplication.consumer;
        var vin = dataSubject.getVin();

        var firstConsentRequest = ConsentRequestSteps.createConsentRequestWithVINFor(providerApplication, vin);
        var consentRequestId1 = firstConsentRequest.getConsentRequestId();
        cridsToRemove.add(consentRequestId1);
        var secondConsentRequest = ConsentRequestSteps.createConsentRequestWithVINFor(providerApplication, vin);
        var consentRequestId2 = secondConsentRequest.getConsentRequestId();
        cridsToRemove.add(consentRequestId2);

        userAccountController.attachConsumerToUserAccount(consentRequestId1, dataSubject.getBearerToken());
        userAccountController.attachVinToUserAccount(vin, dataSubject.getBearerToken());

        open(ConsentPageUrl.getEnvUrlRoot());
        HereLoginSteps.loginDataSubject(dataSubject);

        $(".offers-list").waitUntil(Condition.visible, 10000);
        dataSubject.setBearerToken(getUICmToken());
        openDashboardNewTab();
        verifyConsentOfferTab(0, mpConsumer, secondConsentRequest, vin, PENDING);
        verifyConsentOfferTab(1, mpConsumer, firstConsentRequest, vin, PENDING);

        ConsentFlowSteps.approveConsentForVIN(consentRequestId1, testContainer, vin);
        ConsentFlowSteps.approveConsentForVIN(consentRequestId2, testContainer, vin);

        openDashboardAcceptedTab();
        verifyConsentOfferTab(0, mpConsumer, secondConsentRequest, vin, APPROVED);
        verifyConsentOfferTab(1, mpConsumer, firstConsentRequest, vin, APPROVED);

        ConsentFlowSteps.revokeConsentForVIN(consentRequestId1, vin);
        ConsentFlowSteps.revokeConsentForVIN(consentRequestId2, vin);

        openDashboardRevokedTab();
        verifyConsentOfferTab(0, mpConsumer, secondConsentRequest, vin, REVOKED);
        verifyConsentOfferTab(1, mpConsumer, firstConsentRequest, vin, REVOKED);
    }

    @Step
    private void openDashboardNewTab() {
        $("lui-tab[data-cy='new']").click();
    }

    @Step
    private void openDashboardRevokedTab() {
        $("lui-tab[data-cy='revoked']").click();
    }

    @Step
    private void openDashboardAcceptedTab() {
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
            String expectedText = APPROVED.equals(status) ? "ACCEPTED" : "REVOKED";
            offerBox.$("lui-status").shouldHave(Condition.text(expectedText));
        } else {
            offerBox.$("lui-status").shouldNotBe(Condition.visible);
        }
    }

}
