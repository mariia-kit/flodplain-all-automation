package com.here.platform.common.extensions;

import com.here.platform.cm.rest.model.ConsentRequestData;
import com.here.platform.cm.steps.api.RemoveEntitiesSteps;
import com.here.platform.common.VinsToFile;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;


public class ConsentRequestCascadeRemoveExtension implements AfterEachCallback {

    private final List<String> cridsToRemove = new ArrayList<>();
    private File fileWithVINs;
    private ConsentRequestData consentRequestData;

    public ConsentRequestCascadeRemoveExtension cridToRemove(String cridToRemove) {
        this.cridsToRemove.add(cridToRemove);
        return this;
    }

    public ConsentRequestCascadeRemoveExtension vinToRemove(String vinToRemove) {
        fileWithVINsToRemove(new VinsToFile(vinToRemove).csv());
        return this;
    }

    public ConsentRequestCascadeRemoveExtension fileWithVINsToRemove(File fileWithVINs) {
        this.fileWithVINs = fileWithVINs;
        return this;
    }

    public ConsentRequestCascadeRemoveExtension consentRequestToCleanUp(ConsentRequestData consentRequestData) {
        this.consentRequestData = consentRequestData;
        return this;
    }

    @Override
    public void afterEach(ExtensionContext context) {
        for (String crid : cridsToRemove) {
            RemoveEntitiesSteps.cascadeForceRemoveConsentRequest(crid, fileWithVINs, consentRequestData);
        }
    }

}
