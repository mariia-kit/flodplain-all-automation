package com.here.platform.cm.ui;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.open;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.Condition;
import com.here.platform.common.annotations.CMFeatures.UserAccount;
import com.here.platform.cm.controllers.UserAccountController;
import com.here.platform.cm.enums.ProviderApplications;
import com.here.platform.cm.rest.model.ConsentInfo;
import com.here.platform.cm.steps.ConsentFlowSteps;
import com.here.platform.cm.steps.ConsentRequestSteps;
import com.here.platform.common.VIN;
import io.qameta.allure.Issue;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


@UserAccount
@Tag("ui")
@DisplayName("User Account")
public class UserAccountUITests extends BaseUITests {

    private final UserAccountController userAccountController = new UserAccountController();
    private final ProviderApplications targetApp = ProviderApplications.DAIMLER_CONS_1;
    private final List<String> vinsToRemove = new ArrayList<>();
    private ConsentInfo consentRequestInfo;
    private String crid;

    @BeforeEach
    void createApproveConsentForUser() {
        consentRequestInfo = ConsentRequestSteps.createConsentRequestWithVINFor(targetApp, dataSubject.vin);
        crid = consentRequestInfo.getConsentRequestId();
        userAccountController.attachConsumerToUserAccount(crid, dataSubject.getBearerToken());
        userAccountController.attachVinToUserAccount(dataSubject.vin, dataSubject.getBearerToken());
        vinsToRemove.add(dataSubject.vin);

        consentRequestInfo.resources(targetApp.container.resources);
        ConsentFlowSteps.approveConsentForVIN(crid, testContainer, dataSubject.vin);
    }

    @AfterEach
    void cleanUp() {
        for (String vin : vinsToRemove) {
            userAccountController.deleteVINForUser(vin, dataSubject.getBearerToken());
        }
        userAccountController.deleteConsumerForUser(targetApp.consumer.getRealm(), dataSubject.getBearerToken());
    }

    @Test
    @Issue("NS-1475")
    @DisplayName("Second time opened the approved consent request link for registered user")
    void secondTimeOpenTheApprovedConsentLinkForRegisteredUserTest() {
        open(crid);
        loginDataSubjectHERE(dataSubject);
        $(".offer-box .offer-title").shouldHave(Condition.text(consentRequestInfo.getTitle()));
        dataSubject.setBearerToken(getUICmToken());
        $("lui-status").shouldHave(Condition.textCaseSensitive("ACCEPTED"));
        $(".offer-box").click();
        $$(".container-content [data-cy='resource']")
                .shouldHave(CollectionCondition.textsInAnyOrder(consentRequestInfo.getResources()));
    }

    @Test
    @Issue("NS-1475")
    @DisplayName("Second time opened the approved consent and proceed with new vehicle")
    void openSecondTimeApprovedConsentAndProceedWithNewVehicleTest() {
        var secondVIN = VIN.generate(targetApp.provider.vinLength);
        userAccountController.attachVinToUserAccount(secondVIN, dataSubject.getBearerToken());
        ConsentRequestSteps.addVINsToConsentRequest(targetApp, crid, secondVIN);
        vinsToRemove.add(secondVIN);

        open(crid);
        loginDataSubjectHERE(dataSubject);

        $(".vin-code", 1).shouldHave(Condition.text(new VIN(secondVIN).label()));
        dataSubject.setBearerToken(getUICmToken());
    }

}