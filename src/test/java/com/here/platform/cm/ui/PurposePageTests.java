package com.here.platform.cm.ui;

import static com.codeborne.selenide.Selenide.open;

import com.here.platform.cm.dataAdapters.ConsentInfoToConsentRequestData;
import com.here.platform.cm.enums.ConsentPageUrl;
import com.here.platform.cm.enums.ConsentRequestContainer;
import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.enums.ProviderApplications;
import com.here.platform.cm.pages.PurposePage;
import com.here.platform.cm.rest.model.ConsentInfo;
import com.here.platform.cm.steps.api.ConsentRequestSteps;
import com.here.platform.cm.steps.api.UserAccountSteps;
import com.here.platform.common.extensions.ConsentRequestCascadeRemoveExtension;
import com.here.platform.dataProviders.daimler.DataSubjects;
import com.here.platform.hereAccount.ui.HereLoginSteps;
import com.here.platform.ns.helpers.authentication.AuthController;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;


@DisplayName("Purpose page")
public class PurposePageTests extends BaseUITests {

    private final ProviderApplications providerApplicationForPurpose = ProviderApplications.DAIMLER_CONS_1;
    private final ConsentRequestContainer generatedContainerForPurpose =
            ConsentRequestContainers.generateNew(providerApplicationForPurpose.provider);
    @RegisterExtension
    ConsentRequestCascadeRemoveExtension cascadeRemoveExtension = new ConsentRequestCascadeRemoveExtension();
    private DataSubjects registeredDataSubject;
    private ConsentInfo testConsentRequest;
    private PurposePage purposePage = new PurposePage();

    @BeforeEach
    void beforeEach() {
        registeredDataSubject = DataSubjects.getNextBy18VINLength();

        UserAccountSteps.removeVINFromDataSubject(registeredDataSubject);
        UserAccountSteps.attachDataSubjectVINToUserAccount(registeredDataSubject);

        testConsentRequest = ConsentRequestSteps.createValidConsentRequestWithNSOnboardings(
                providerApplicationForPurpose,
                registeredDataSubject.getVin(),
                generatedContainerForPurpose
        );

        var consentReqToRemove = new ConsentInfoToConsentRequestData(
                testConsentRequest,
                providerApplicationForPurpose.provider.getName(),
                providerApplication.consumer.getRealm()
        ).consentRequestData();

        cascadeRemoveExtension.consentRequestToCleanUp(consentReqToRemove);
        cascadeRemoveExtension.vinToRemove(registeredDataSubject.getVin());

    }

    @AfterEach
    void afterEach() {
        AuthController.deleteToken(registeredDataSubject.dataSubject);
        UserAccountSteps.removeVINFromDataSubject(registeredDataSubject);
    }


    @Test
    @DisplayName("Verify Purpose page for registeredAccount")
    void verifyPurposePageTest() {
        open(ConsentPageUrl.getStaticPurposePageLinkFor(
                providerApplicationForPurpose.consumer.getRealm(),
                generatedContainerForPurpose.getId())
        );

        purposePage.verifyStaticPurposeInfoPage();
        purposePage.openConsentRequestLink();
        HereLoginSteps.loginRegisteredDataSubject(registeredDataSubject.dataSubject);
        purposePage.verifyPurposeInfoPage(
                providerApplicationForPurpose.consumer,
                testConsentRequest,
                generatedContainerForPurpose
        );
    }




}
