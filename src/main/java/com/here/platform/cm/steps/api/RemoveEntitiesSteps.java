package com.here.platform.cm.steps.api;

import com.here.platform.cm.controllers.ConsentRequestController;
import com.here.platform.cm.controllers.PrivateController;
import com.here.platform.cm.enums.MPConsumers;
import com.here.platform.cm.rest.model.ConsentRequestData;
import io.qameta.allure.Step;
import java.io.File;
import lombok.experimental.UtilityClass;


@UtilityClass
public class RemoveEntitiesSteps {

    private final PrivateController privateController = new PrivateController();
    private final ConsentRequestController consentRequestController = new ConsentRequestController();


    @Step
    public void cascadeForceRemoveConsentRequest(
            String crid,
            File fileWithVINs,
            ConsentRequestData consentRequestData
    ) {
        if (crid != null) {
            forceRemoveConsentRequestWithConsents(crid, fileWithVINs);
            forceRemoveApplicationProviderConsumerEntities(consentRequestData);
        }
    }

    @Step
    public void forceRemoveConsentRequestWithConsents(String crid, File fileWithVINs) {
        if (crid == null) {
            return;
        }
        if (fileWithVINs != null) {
            consentRequestController
                    .withConsumerToken(MPConsumers.OLP_CONS_1)
                    .forceRemoveVinsFromConsentRequest(crid, fileWithVINs);
        }
        var deleteConsentRequestResponse = privateController
                .withCMToken()
                .hardDeleteConsentRequest(crid);
        StepExpects.expectNOCONSTENTStatusCode(deleteConsentRequestResponse);
    }

    @Step
    public void forceRemoveApplicationProviderConsumerEntities(ConsentRequestData consentRequestData) {
        if (consentRequestData == null) {
            return;
        }

        privateController.withCMToken();

        var deleteProviderApplicationResponse = privateController.deleteProviderApplication(
                consentRequestData.getProviderId(),
                consentRequestData.getConsumerId(),
                consentRequestData.getContainerId()
        );
        StepExpects.expectNOCONSTENTStatusCode(deleteProviderApplicationResponse);

        removeProvider(consentRequestData.getProviderId());

        removeConsumer(consentRequestData.getConsumerId());
    }

    public void removeProvider(String providerId) {
        privateController.withCMToken();
        var deleteProviderResponse = privateController.deleteProvider(providerId);
        StepExpects.expectNOCONSTENTStatusCode(deleteProviderResponse);
    }

    public void removeConsumer(String consumerId) {
        privateController.withCMToken();
        var deleteConsumerResponse = privateController.deleteConsumer(consumerId);
        StepExpects.expectNOCONSTENTStatusCode(deleteConsumerResponse);
    }

}
