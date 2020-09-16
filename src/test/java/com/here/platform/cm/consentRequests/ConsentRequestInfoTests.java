package com.here.platform.cm.consentRequests;

import com.here.platform.cm.BaseCMTest;
import com.here.platform.cm.controllers.ConsentStatusController;
import com.here.platform.cm.controllers.ConsentStatusController.PageableConsent;
import com.here.platform.cm.enums.ConsentRequestContainers;
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
import com.here.platform.common.extensions.ConsentRequestRemoveExtension;
import com.here.platform.common.extensions.OnboardAndRemoveApplicationExtension;
import com.here.platform.common.extensions.ProviderApplicationRemoveExtension;
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
    private final ConsentRequestContainers testContainer = ConsentRequestContainers.DAIMLER_EXPERIMENTAL_FUEL;
    private final ConsentRequestData testConsentRequest = new ConsentRequestData()
            .consumerId(mpConsumer.getRealm())
            .providerId(testContainer.provider.getName())
            .purpose(faker.commerce().productName())
            .privacyPolicy(faker.internet().url())
            .containerId(testContainer.id);
    @RegisterExtension
    final OnboardAndRemoveApplicationExtension applicationExtension =
            OnboardAndRemoveApplicationExtension.builder().consentRequestData(testConsentRequest).build();

    @RegisterExtension
    final ProviderApplicationRemoveExtension applicationRemoveExtension =
            ProviderApplicationRemoveExtension.builder().consentRequestData(testConsentRequest).build();

    private final ConsentStatusController consentStatusController = new ConsentStatusController();

    private String createConsentRequestWithCar(String targetVin) {
        consentRequestController.withConsumerToken();
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
