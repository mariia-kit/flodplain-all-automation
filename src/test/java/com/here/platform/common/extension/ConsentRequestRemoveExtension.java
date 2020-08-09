package com.here.platform.common.extension;

import com.here.platform.cm.steps.api.RemoveEntitiesSteps;
import com.here.platform.common.VinsToFile;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;


public class ConsentRequestRemoveExtension implements AfterEachCallback {

    private final List<String> cridsToRemove = new ArrayList<>();
    private File fileWithVINs;

    public ConsentRequestRemoveExtension cridToRemove(String cridToRemove) {
        this.cridsToRemove.add(cridToRemove);
        return this;
    }

    public ConsentRequestRemoveExtension vinToRemove(String vinToRemove) {
        fileWithVINsToRemove(new VinsToFile(vinToRemove).csv());
        return this;
    }

    public ConsentRequestRemoveExtension fileWithVINsToRemove(File fileWithVINs) {
        this.fileWithVINs = fileWithVINs;
        return this;
    }

    public ConsentRequestRemoveExtension consentRequestToRemove(String cridToRemove, File fileWithVINsToRemove) {
        cridToRemove(cridToRemove);
        fileWithVINsToRemove(fileWithVINsToRemove);
        return this;
    }

    @Override
    public void afterEach(ExtensionContext context) {
        for (String crid : cridsToRemove) {
            RemoveEntitiesSteps.forceRemoveConsentRequestWithConsents(crid, fileWithVINs);
        }
    }

}
