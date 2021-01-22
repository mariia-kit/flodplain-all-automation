package com.here.platform.cm.steps.api;

import com.here.platform.cm.controllers.ConsentRequestController;
import com.here.platform.cm.controllers.PrivateController;
import com.here.platform.cm.rest.model.ConsentRequestData;
import com.here.platform.cm.rest.model.ProviderApplication;
import com.here.platform.common.VinsToFile;
import com.here.platform.ns.dto.Users;
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
                    .withAuthorizationValue(Users.MP_CONSUMER.getToken())
                    .forceRemoveVinsFromConsentRequest(crid, fileWithVINs);
        }
        var deleteConsentRequestResponse = privateController
                .withCMToken()
                .hardDeleteConsentRequest(crid);
        StatusCodeExpects.expectNOCONSTENTStatusCode(deleteConsentRequestResponse);
    }

    @Step("Remove consent for {vin} from consent request {crid}")
    public void forceRemoveConsentsFromConsentRequest(String crid, String vin) {
        if (crid == null) {
            return;
        }
        consentRequestController
                .withAuthorizationValue(Users.MP_CONSUMER.getToken())
                .forceRemoveVinsFromConsentRequest(crid, new VinsToFile(vin).json());
    }

    @Step("Clean empty consent request {crid}")
    public void forceRemoveEmptyConsentRequest(String crid) {
        if (crid == null) {
            return;
        }
        var deleteConsentRequestResponse = privateController
                .withCMToken()
                .hardDeleteConsentRequest(crid);
        StatusCodeExpects.expectNOCONSTENTStatusCode(deleteConsentRequestResponse);
    }

    @Step
    public void forceRemoveApplicationProviderConsumerEntities(ConsentRequestData consentRequestData) {
        if (consentRequestData == null) {
            return;
        }

        var appToRemove = new ProviderApplication()
                .consumerId(consentRequestData.getConsumerId())
                .providerId(consentRequestData.getProviderId())
                .containerId(consentRequestData.getContainerId());
        removeProviderApplication(appToRemove);

        removeProvider(consentRequestData.getProviderId());

        removeConsumer(consentRequestData.getConsumerId());
    }

    public void removeProviderApplication(ProviderApplication providerApplication) {
        privateController.withCMToken();
        var deleteProviderAppResponse = privateController.deleteProviderApplication(providerApplication);
        StatusCodeExpects.expectNOCONSTENTStatusCode(deleteProviderAppResponse);
    }

    public void removeProvider(String providerId) {
        if (providerId == null) {
            return;
        }
        privateController.withCMToken();
        var deleteProviderResponse = privateController.deleteProvider(providerId);
        StatusCodeExpects.expectNOCONSTENTStatusCode(deleteProviderResponse);
    }

    public void removeConsumer(String consumerId) {
        if (consumerId == null) {
            return;
        }
        privateController.withCMToken();
        var deleteConsumerResponse = privateController.deleteConsumer(consumerId);
        StatusCodeExpects.expectNOCONSTENTStatusCode(deleteConsumerResponse);
    }

}
