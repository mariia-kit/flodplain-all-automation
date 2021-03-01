package com.here.platform.proxy.helper;

import com.here.platform.cm.steps.remove.DataForRemoveCollector;
import com.here.platform.mp.steps.api.MarketplaceSteps;
import com.here.platform.proxy.conrollers.ServiceProvidersController;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;


public class ProxyRemoveExtension implements AfterEachCallback {

    @Override
    @Step("Clean Up Proxy test data")
    public void afterEach(ExtensionContext context) {
        String testId = Allure.getLifecycle().getCurrentTestCase().get();
        DataForRemoveCollector
                .getAllMpSubs(testId)
                .forEach(id -> new MarketplaceSteps().beginCancellation(id));
        DataForRemoveCollector
                .getAllMpListings(testId)
                .forEach(id -> new MarketplaceSteps().deleteListing(id));

        RemoveObjCollector
                .getAllProxyProvidersWithResources(testId)
                .forEach(value -> new ServiceProvidersController()
                        .withAdminToken()
                        .deleteResourceFromProvider(value.getId()));
        RemoveObjCollector
                .getAllProxyProviders(testId)
                .forEach(id -> new ServiceProvidersController()
                        .withAdminToken()
                        .deleteProviderById(id));
    }

}
