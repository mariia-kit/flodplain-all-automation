package com.here.platform.ns.dto;

import com.here.platform.ns.helpers.LoggerHelper;
import com.here.platform.ns.helpers.ContainerResourcesHelper;
import com.here.platform.ns.helpers.UniqueId;
import com.here.platform.ns.utils.NS_Config;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum Providers {
    DAIMLER_EXPERIMENTAL(new DataProvider("daimler_experimental", "https://api.mercedes-benz.com/experimental/connectedvehicle/v1")
            .withResources(ContainerResources.ODOMETER.getResource())
            .withResources(ContainerResources.FUEL.getResource())
            .withResources(ContainerResources.CHARGE.getResource())
            .withResources(ContainerResources.TIRES.getResource())
            .withResources(ContainerResources.DOORS.getResource())
            .withResources(new ProviderResource("oil"))),
    BMW(new DataProvider("bmw", "http://www.bmw.com")),
    NOT_EXIST(new DataProvider("rimak", "http://www.rim.com")),
    DAIMLER_CAPITAL(new DataProvider("Daimler", "https://api.mercedes-benz.com/vehicledata/v1")),
    REFERENCE_PROVIDER(new DataProvider(NS_Config.REFERENCE_J_PROV_NAME.toString(), NS_Config.REFERENCE_J_PROV_URL.toString())
            .withResources(ContainerResources.ODOMETER.getResource())
            .withResources(ContainerResources.FUEL.getResource())
            .withResources(ContainerResources.CHARGE.getResource())
            .withResources(ContainerResources.TIRES.getResource())
            .withResources(ContainerResources.DOORS.getResource())
            .withResources(new ProviderResource("payasyoudrive"))
            .withResources(new ProviderResource("fuelstatus"))
    ),
    DAIMLER_REFERENCE(new DataProvider("daimleR_experimental", NS_Config.REFERENCE_J_PROV_URL.toString())
            .withResources(ContainerResources.ODOMETER.getResource())
            .withResources(ContainerResources.FUEL.getResource())
            .withResources(ContainerResources.CHARGE.getResource())
            .withResources(ContainerResources.TIRES.getResource())
            .withResources(ContainerResources.DOORS.getResource())
            .withResources(new ProviderResource("oil"))
            .withResources(new ProviderResource("vehicles"))
    ),
    DAIMLER_REAL(new DataProvider("daimler", "https://api.mercedes-benz.com/vehicledata/v1")),
    REFERENCE_PROVIDER_PROD(new DataProvider("reference_provider", NS_Config.REFERENCE_J_PROV_URL.toString())
            .withResources(ContainerResources.ODOMETER.getResource())
            .withResources(ContainerResources.FUEL.getResource())
            .withResources(ContainerResources.CHARGE.getResource())
            .withResources(ContainerResources.TIRES.getResource())
            .withResources(ContainerResources.DOORS.getResource())
            .withResources(new ProviderResource("payasyoudrive"))
            .withResources(new ProviderResource("fuelstatus"))
    );

    private DataProvider provider;

    public static String getDataProviderNamePrefix() {
        return "AutomatedTestProvider";
    }

    public static DataProvider generateNew() {
        DataProvider dataProvider = new DataProvider(
                getDataProviderNamePrefix() + UniqueId.getUniqueKey(),
                "http://www.here.com");
        dataProvider.addResource(ContainerResourcesHelper.generateOdometer());
        LoggerHelper.logStep("Generate new DataProvider:" + dataProvider.toString());
        return dataProvider;
    }

    public String getName() {
        return this.getProvider().getName();
    }
}
