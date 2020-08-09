package com.here.platform.cm.consentRequests;

import com.here.platform.cm.BaseCMTest;
import com.here.platform.cm.controllers.ConsentStatusController;
import com.here.platform.cm.controllers.ConsentStatusController.PageableConsent;
import com.here.platform.cm.enums.DaimlerContainers;
import com.here.platform.cm.enums.MPConsumers;
import com.here.platform.cm.rest.model.ConsentInfo;
import com.here.platform.cm.rest.model.ConsentInfo.StateEnum;
import com.here.platform.cm.rest.model.ConsentRequestData;
import com.here.platform.cm.rest.model.ConsentRequestIdResponse;
import com.here.platform.common.ResponseAssertion;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import com.here.platform.common.VIN;
import com.here.platform.common.VinsToFile;
import com.here.platform.common.annotations.CMFeatures.CreateConsentRequest;
import com.here.platform.common.extension.ConsentRequestRemoveExtension;
import com.here.platform.common.extension.OnboardApplicationExtension;
import com.here.platform.common.extension.ProviderApplicationRemoveExtension;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;


@CreateConsentRequest
@DisplayName("Consent request Info")
public class ConsentRequestInfoTests extends BaseCMTest {

    @RegisterExtension
    final ConsentRequestRemoveExtension requestRemoveExtension = new ConsentRequestRemoveExtension();

    private final MPConsumers mpConsumer = MPConsumers.OLP_CONS_1;
    private final DaimlerContainers testContainer = DaimlerContainers.DAIMLER_EXPERIMENTAL_FUEL;
    private final ConsentRequestData testConsentRequest = new ConsentRequestData()
            .consumerId(mpConsumer.getRealm())
            .providerId(testContainer.provider.getName())
            .purpose(faker.commerce().productName())
            .privacyPolicy(faker.internet().url())
            .containerId(testContainer.id);
    @RegisterExtension
    final OnboardApplicationExtension applicationExtension =
            OnboardApplicationExtension.builder().consentRequestData(testConsentRequest).build();

    @RegisterExtension
    final ProviderApplicationRemoveExtension applicationRemoveExtension =
            ProviderApplicationRemoveExtension.builder().consentRequestData(testConsentRequest).build();

    private final ConsentStatusController consentStatusController = new ConsentStatusController();

    @Test
    @DisplayName("Fetch consent request info for VIN")
    void fetchConsentRequestInfoTest() {
        var testVin = VIN.generate(18);
        var theLast8SymbolsOfVin = new VIN(testVin).label();

        var firstTitle = faker.gameOfThrones().quote();
        testConsentRequest.title(firstTitle);
        var firstCrid = createConsentRequestWithCar(testVin);
        requestRemoveExtension.cridToRemove(firstCrid);

        var firstExpectedInfos = new ConsentInfo()
                .consentRequestId(firstCrid)
                .purpose(testConsentRequest.getPurpose())
                .title(firstTitle)
                .consumerName(mpConsumer.getConsumerName())
                .state(StateEnum.PENDING)
                .approveTime(null)
                .revokeTime(null)
                .containerName(testContainer.name)
                .containerDescription(testContainer.containerDescription)
                .resources(testContainer.resources)
                .vinLabel(theLast8SymbolsOfVin);

        var consentRequestInfoResponse = consentStatusController
                .getConsentsInfo(
                        testVin,
                        PageableConsent.builder().stateEnum(StateEnum.PENDING).build()
                );
        var consentRequestInfos = new ResponseAssertion(consentRequestInfoResponse).statusCodeIsEqualTo(StatusCode.OK)
                .bindAsListOf(ConsentInfo[].class);
        Assertions.assertThat(consentRequestInfos)
                .usingElementComparatorIgnoringFields("createTime", "vinHash")
                .hasSize(1)
                .contains(firstExpectedInfos);

        var secondTitle = faker.gameOfThrones().quote();
        testConsentRequest.title(secondTitle);
        var secondCrid = createConsentRequestWithCar(testVin);
        requestRemoveExtension.cridToRemove(secondCrid);

        var secondExpectedInfo = new ConsentInfo()
                .title(secondTitle).consentRequestId(secondCrid)
                .consumerName(mpConsumer.getConsumerName())
                .containerName(testContainer.name)
                .containerDescription(testContainer.containerDescription)
                .resources(firstExpectedInfos.getResources())
                .purpose(testConsentRequest.getPurpose()).state(StateEnum.PENDING)
                .vinLabel(theLast8SymbolsOfVin);

        consentRequestInfoResponse = consentStatusController
                .getConsentsInfo(
                        testVin,
                        PageableConsent.builder().stateEnum(StateEnum.PENDING).build()
                );
        consentRequestInfos = new ResponseAssertion(consentRequestInfoResponse)
                .statusCodeIsEqualTo(StatusCode.OK)
                .bindAsListOf(ConsentInfo[].class);
        Assertions.assertThat(consentRequestInfos)
                .usingElementComparatorIgnoringFields("createTime", "vinHash")
                .hasSize(2)
                .containsExactlyInAnyOrder(firstExpectedInfos, secondExpectedInfo);
    }


    private String createConsentRequestWithCar(String targetVin) {
        consentRequestController.withCMToken();
        var crid = new ResponseAssertion(consentRequestController.createConsentRequest(testConsentRequest))
                .statusCodeIsEqualTo(StatusCode.CREATED)
                .bindAs(ConsentRequestIdResponse.class)
                .getConsentRequestId();

        fuSleep();
        var testFileWithVINs = new VinsToFile(targetVin).json(); //save for remove

        var addVinsResponse = consentRequestController
                .withConsumerToken(mpConsumer)
                .addVinsToConsentRequest(crid, testFileWithVINs);
        new ResponseAssertion(addVinsResponse).statusCodeIsEqualTo(StatusCode.OK);

        requestRemoveExtension.fileWithVINsToRemove(testFileWithVINs);
        return crid;
    }

}
