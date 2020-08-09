package com.here.platform.common.extension;

import com.here.platform.cm.rest.model.ConsentRequestData;
import com.here.platform.cm.steps.api.RemoveEntitiesSteps;
import java.io.File;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;


public class ConsentRequestCascadeRemoveExtension implements AfterEachCallback {

    private String crid;
    private File fileWithVINs;
    private ConsentRequestData consentRequestData;

    public ConsentRequestCascadeRemoveExtension cascadeRemove(
            String crid,
            File fileWithVINs,
            ConsentRequestData consentRequestData
    ) {
        this.crid = crid;
        this.fileWithVINs = fileWithVINs;
        this.consentRequestData = consentRequestData;
        return this;
    }

    @Override
    public void afterEach(ExtensionContext context) {
        RemoveEntitiesSteps.cascadeForceRemoveConsentRequest(crid, fileWithVINs, consentRequestData);
    }

}
