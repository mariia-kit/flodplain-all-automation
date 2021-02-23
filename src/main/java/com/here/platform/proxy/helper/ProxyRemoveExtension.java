package com.here.platform.proxy.helper;

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
        RemoveObjCollector
                .getAllProxyProvidersWithResources(testId)
                .forEach(value -> new ServiceProvidersController()
                        .withAppToken()
                        .deleteResourceFromProvider(value.getValue()));
        RemoveObjCollector
                .getAllProxyProviders(testId)
                .forEach(id -> new ServiceProvidersController()
                        .withAppToken()
                        .deleteProviderById(id));
    }

}
