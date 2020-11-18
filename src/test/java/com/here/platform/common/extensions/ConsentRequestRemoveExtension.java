package com.here.platform.common.extensions;

import com.here.platform.cm.steps.api.RemoveEntitiesSteps;
import com.here.platform.common.VinsToFile;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;


public class ConsentRequestRemoveExtension implements AfterEachCallback {

    private final List<String> cridsToRemove = new ArrayList<>();
    private List<String> vins = new ArrayList<>();

    public ConsentRequestRemoveExtension cridToRemove(String cridToRemove) {
        this.cridsToRemove.add(cridToRemove);
        return this;
    }

    public ConsentRequestRemoveExtension vinToRemove(String... vinToRemove) {
        for (String vin: vinToRemove) {
            vins.add(vin);
        }
        return this;
    }

    public ConsentRequestRemoveExtension consentRequestToRemove(String cridToRemove, String vinToRemove) {
        cridToRemove(cridToRemove);
        vinToRemove(vinToRemove);
        return this;
    }

    @Override
    public void afterEach(ExtensionContext context) {
        for (String crid : cridsToRemove) {
            RemoveEntitiesSteps.forceRemoveConsentRequestWithConsents(crid, new VinsToFile(vins.toArray(new String[vins.size()])).csv());
        }
    }

}
