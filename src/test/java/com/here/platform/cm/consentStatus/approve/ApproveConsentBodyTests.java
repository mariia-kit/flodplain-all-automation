package com.here.platform.cm.consentStatus.approve;

import com.here.platform.cm.consentStatus.BaseConsentStatusTests;
import com.here.platform.cm.controllers.ConsentStatusController.NewConsent;
import com.here.platform.cm.rest.model.ConsentInfo;
import com.here.platform.cm.rest.model.ConsentInfo.StateEnum;
import com.here.platform.cm.rest.model.SuccessApproveData;
import com.here.platform.cm.steps.api.RemoveEntitiesSteps;
import com.here.platform.common.ResponseAssertion;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import com.here.platform.common.VIN;
import com.here.platform.common.annotations.CMFeatures.ApproveConsent;
import com.here.platform.dataProviders.daimler.DaimlerTokenController;
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
@DisplayName("Approve consent")
@Execution(ExecutionMode.SAME_THREAD)
public class ApproveConsentBodyTests extends BaseConsentStatusTests {

    private final String privateBearer = dataSubject.getBearerToken();
    private final List<String> cridsToRemove = new ArrayList<>();
    private NewConsent consentToApprove;
    private String crid;

    @BeforeEach
    void createAndApproveConsent() {
        crid = createValidConsentRequest();
        cridsToRemove.add(crid);
        var validDaimlerToken = new DaimlerTokenController(testVin, testContainer).generateAuthorizationCode();
        this.consentToApprove = NewConsent.builder()
                .vinHash(new VIN(testVin).hashed())
                .consentRequestId(crid)
                .authorizationCode(validDaimlerToken)
                .build();

    }

    @AfterEach
    void cleanUp() {
        for (String cridItem : cridsToRemove) {
            RemoveEntitiesSteps.forceRemoveConsentRequestWithConsents(cridItem, testFileWithVINs);
        }
        RemoveEntitiesSteps.forceRemoveApplicationProviderConsumerEntities(testConsentRequestData);
    }

    @Test
    @DisplayName("Approve consent with single pending consent")
    void approveConsentWithSinglePendingConsentTest() {
        var cridForPendingConsent = createValidConsentRequest();

        var approveConsentResponse = consentStatusController.approveConsent(consentToApprove, privateBearer);

        var successApproveData = new ResponseAssertion(approveConsentResponse)
                .statusCodeIsEqualTo(StatusCode.OK)
                .bindAs(SuccessApproveData.class);

        Assertions.assertThat(successApproveData.getRecentPendingConsentsInfo())
                .usingElementComparatorIgnoringFields("createTime", "vinHash")
                .contains(new ConsentInfo()
                        .consentRequestId(cridForPendingConsent)
                        .consumerName(mpConsumer.getConsumerName())
                        .title(testConsentRequestData.getTitle())
                        .purpose(testConsentRequestData.getPurpose())
                        .vinLabel(new VIN(testVin).label())
                        .containerName(testContainer.name)
                        .containerDescription(testContainer.containerDescription)
                        .resources(testContainer.resources)
                        .state(StateEnum.PENDING)
                        .approveTime(null)
                        .revokeTime(null)
                );
    }

    @Test
    @DisplayName("Approve consent with max pending list of consents")
    void approveConsentWithMaxPendingListConsentsTest() {
        var secondPendingConsent = createValidConsentRequest();
        cridsToRemove.add(secondPendingConsent);

        var thirdPendingCrid = createValidConsentRequest();
        cridsToRemove.add(thirdPendingCrid);

        var approveConsentResponse = consentStatusController.approveConsent(consentToApprove, privateBearer);

        var successApproveData = new ResponseAssertion(approveConsentResponse)
                .statusCodeIsEqualTo(StatusCode.OK)
                .bindAs(SuccessApproveData.class);

        Assertions.assertThat(successApproveData.getRecentPendingConsentsInfo())
                .hasSize(2);

        var consentRequestIds = successApproveData.getRecentPendingConsentsInfo().stream()
                .map(ConsentInfo::getConsentRequestId)
                .collect(Collectors.toList());

        Assertions.assertThat(consentRequestIds).isEqualTo(List.of(thirdPendingCrid, secondPendingConsent));
    }

}
