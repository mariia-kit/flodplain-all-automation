package com.here.platform.common.extensions;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;


public class ConsentManagementExtension implements BeforeEachCallback, AfterEachCallback {

    //todo create dummy consent request
    //todo onboard dummy provider, consumer, application
    //todo remove provider, consumer, application

    @Override
    public void afterEach(ExtensionContext context) {

    }

    @Override
    public void beforeEach(ExtensionContext context) {

    }

}
