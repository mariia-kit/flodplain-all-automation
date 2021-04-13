package com.here.platform.cm.ui;

import static com.codeborne.selenide.Selenide.open;

import com.here.platform.cm.enums.ConsentObject;
import com.here.platform.cm.enums.ConsentPageUrl;
import com.here.platform.cm.enums.ConsentRequestContainer;
import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.enums.MPProviders;
import com.here.platform.cm.pages.PurposePage;
import com.here.platform.cm.steps.api.ConsentRequestSteps;
import com.here.platform.cm.steps.api.UserAccountSteps;
import com.here.platform.common.annotations.CMFeatures.Purpose;
import com.here.platform.dataProviders.daimler.DataSubjects;
import com.here.platform.hereAccount.ui.HereLoginSteps;
import com.here.platform.ns.dto.User;
import com.here.platform.ns.dto.Users;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


@Purpose
@DisplayName("[UI] Purpose for consent request")
public class PurposePageTests extends BaseUITests {

    private final PurposePage purposePage = new PurposePage();

    @Test
    @DisplayName("Verify Purpose page for registered account")
    void verifyPurposePageTest() {
        MPProviders provider = MPProviders.DAIMLER_EXPERIMENTAL;
        User mpConsumer = Users.MP_CONSUMER.getUser();
        ConsentRequestContainer targetContainer = ConsentRequestContainers.generateNew(provider);
        ConsentObject consentObj = new ConsentObject(mpConsumer, provider, targetContainer);

        DataSubjects registeredDataSubject = DataSubjects.getNextBy18VINLength().getNextBy18VINLength();
        UserAccountSteps.removeVINFromDataSubject(registeredDataSubject);
        UserAccountSteps.attachDataSubjectVINToUserAccount(registeredDataSubject.getDataSubject());

        new ConsentRequestSteps(consentObj)
                .onboardAllForConsentRequest()
                .createConsentRequest()
                .addVINsToConsentRequest(registeredDataSubject.getVin())
                .getId();

        open(ConsentPageUrl.getStaticPurposePageLinkFor(
                mpConsumer.getRealm(),
                targetContainer.getId())
        );

        purposePage.verifyStaticPurposeInfoPage();
        purposePage.openConsentRequestLink();
        HereLoginSteps.loginRegisteredDataSubject(registeredDataSubject.dataSubject);
        purposePage.verifyPurposeInfoPage(
                mpConsumer,
                consentObj.getConsent(),
                targetContainer
        );
    }


}
