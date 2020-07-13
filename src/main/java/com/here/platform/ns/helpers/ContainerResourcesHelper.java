package com.here.platform.ns.helpers;

import com.here.platform.ns.dto.ProviderResource;


public class ContainerResourcesHelper {

    public static final String REGULAR_RESOURCE = "odometer";

    public static ProviderResource generateOdometer() {
        ProviderResource res = new ProviderResource(REGULAR_RESOURCE);
        LoggerHelper.logStep("Generate new Resource:" + res.toString());
        return res;
    }

}
