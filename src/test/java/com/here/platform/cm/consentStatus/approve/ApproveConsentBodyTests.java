package com.here.platform.cm.consentStatus.approve;

import com.here.platform.cm.consentStatus.BaseConsentStatusTests;
import com.here.platform.cm.controllers.ConsentStatusController.NewConsent;
import com.here.platform.cm.enums.ConsentObject;
import com.here.platform.cm.enums.ConsentRequestContainer;
import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.enums.MPProviders;
import com.here.platform.cm.rest.model.ConsentInfo.StateEnum;
import com.here.platform.cm.rest.model.SuccessApproveData;
import com.here.platform.cm.steps.api.ConsentRequestSteps;
import com.here.platform.cm.steps.api.UserAccountSteps;
import com.here.platform.common.DataSubject;
import com.here.platform.common.ResponseAssertion;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import com.here.platform.common.annotations.CMFeatures.ApproveConsent;
import com.here.platform.common.strings.VIN;
import com.here.platform.dataProviders.reference.controllers.ReferenceTokenController;
import com.here.platform.ns.dto.User;
import com.here.platform.ns.dto.Users;
import com.here.platform.ns.helpers.authentication.AuthController;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;


@ApproveConsent
@Disabled
@DisplayName("Approve consent Pending Consent list")
@Execution(ExecutionMode.SAME_THREAD)
public class ApproveConsentBodyTests extends BaseConsentStatusTests {

    @Test
    @DisplayName("Approve consent with single pending consent")
    void approveConsentWithSinglePendingConsentTest() {
        MPProviders provider = MPProviders.DAIMLER_REFERENCE;
        User mpConsumer = Users.MP_CONSUMER.getUser();

        ConsentRequestContainer targetContainer1 = ConsentRequestContainers.generateNew(provider);
        ConsentRequestContainer targetContainer2 = ConsentRequestContainers.generateNew(provider);

        DataSubject dataSubject = UserAccountSteps.generateNewHereAccount(provider.getVinLength());
        String vinToApprove = dataSubject.getVin();

        ConsentObject consentObj1 = new ConsentObject(mpConsumer, provider, targetContainer1);
        ConsentObject consentObj2 = new ConsentObject(mpConsumer, provider, targetContainer2);
        var crid1 = new ConsentRequestSteps(consentObj1)
                .onboardAllForConsentRequest()
                .createConsentRequest()
                .addVINsToConsentRequest(vinToApprove)
                .getId();
        var crid2 = new ConsentRequestSteps(consentObj2)
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
                .approveConsent(consentToApprove, AuthController.getDataSubjectToken(dataSubject));

        var successApproveData = new ResponseAssertion(approveConsentResponse)
                .statusCodeIsEqualTo(StatusCode.OK)
                .bindAs(SuccessApproveData.class);

        Assertions.assertThat(successApproveData.getRecentPendingConsentsInfo())
                .usingElementComparatorIgnoringFields("createTime", "vinHash")
                .contains(consentObj1.getConsent()
                        .vinLabel(new VIN(vinToApprove).label())
                        .state(StateEnum.PENDING)
                );
    }

}
