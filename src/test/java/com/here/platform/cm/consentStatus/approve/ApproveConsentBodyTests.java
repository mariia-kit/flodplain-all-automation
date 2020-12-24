package com.here.platform.cm.consentStatus.approve;

import com.here.platform.cm.consentStatus.BaseConsentStatusTests;
import com.here.platform.cm.controllers.ConsentStatusController.NewConsent;
import com.here.platform.cm.enums.ConsentRequestContainer;
import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.enums.Consents;
import com.here.platform.cm.enums.ProviderApplications;
import com.here.platform.cm.rest.model.ConsentInfo;
import com.here.platform.cm.rest.model.ConsentInfo.StateEnum;
import com.here.platform.cm.rest.model.SuccessApproveData;
import com.here.platform.cm.steps.api.ConsentFlowSteps;
import com.here.platform.cm.steps.api.ConsentRequestSteps2;
import com.here.platform.cm.steps.api.RemoveEntitiesSteps;
import com.here.platform.common.DataSubject;
import com.here.platform.common.ResponseAssertion;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import com.here.platform.common.annotations.CMFeatures.ApproveConsent;
import com.here.platform.common.strings.VIN;
import com.here.platform.dataProviders.daimler.DataSubjects;
import com.here.platform.dataProviders.reference.controllers.ReferenceTokenController;
import com.here.platform.hereAccount.controllers.HereUserManagerController.HereUser;
import com.here.platform.ns.dto.User;
import com.here.platform.ns.dto.Users;
import com.here.platform.ns.helpers.authentication.AuthController;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;


@ApproveConsent
@DisplayName("Approve consent Pending Consent list")
@Execution(ExecutionMode.SAME_THREAD)
public class ApproveConsentBodyTests extends BaseConsentStatusTests {
    ProviderApplications targetApp = ProviderApplications.REFERENCE_CONS_1;
    HereUser hereUser = null;
    DataSubject dataSubjectIm;

    @BeforeEach
    void createConsentRequestAndApproveConsent() {

        hereUser = new HereUser(faker.internet().emailAddress(), faker.internet().password(), "here");
        dataSubjectIm = new DataSubject(
                hereUser.getEmail(),
                hereUser.getPassword(),
                VIN.generate(targetApp.getProvider().getVinLength())
        );
        hereUserManagerController.createHereUser(hereUser);
    }

    @AfterEach
    void cleanUp() {
        if (hereUser != null) {
            hereUserManagerController.deleteHereUser(hereUser);
        }
        AuthController.deleteToken(dataSubjectIm);
    }

    @Test
    @DisplayName("Approve consent with single pending consent")
    void approveConsentWithSinglePendingConsentTest() {
        User mpConsumer = Users.MP_CONSUMER.getUser();
        ConsentRequestContainer targetContainer1 = ConsentRequestContainers.generateNew(targetApp.getProvider());
        ConsentRequestContainer targetContainer2 = ConsentRequestContainers.generateNew(targetApp.getProvider());

        String vinToApprove = dataSubjectIm.getVin();

        ConsentInfo consentInfo1 = Consents.generateNewConsentInfo(mpConsumer, targetContainer1);
        ConsentInfo consentInfo2 = Consents.generateNewConsentInfo(mpConsumer, targetContainer2);
        var crid1 = new ConsentRequestSteps2(targetContainer1, consentInfo1)
                .onboardAllForConsentRequest()
                .createConsentRequest()
                .addVINsToConsentRequest(vinToApprove)
                .getId();
        var crid2 = new ConsentRequestSteps2(targetContainer2, consentInfo2)
                .onboardAllForConsentRequest()
                .createConsentRequest()
                .addVINsToConsentRequest(vinToApprove)
                .getId();

        var validRefToken = ReferenceTokenController
                .produceConsentAuthCode(vinToApprove, targetContainer1.getId() + ":general");

        NewConsent consentToApprove = NewConsent.builder()
                .vinHash(new VIN(vinToApprove).hashed())
                .consentRequestId(crid2)
                .authorizationCode(validRefToken)
                .build();

        var approveConsentResponse = consentStatusController
                .withConsumerToken()
                .approveConsent(consentToApprove, AuthController.getDataSubjectToken(dataSubjectIm));

        var successApproveData = new ResponseAssertion(approveConsentResponse)
                .statusCodeIsEqualTo(StatusCode.OK)
                .bindAs(SuccessApproveData.class);

        Assertions.assertThat(successApproveData.getRecentPendingConsentsInfo())
                .usingElementComparatorIgnoringFields("createTime", "vinHash")
                .contains(consentInfo1
                        .vinLabel(new VIN(vinToApprove).label())
                        .state(StateEnum.PENDING)
                );
    }

}
