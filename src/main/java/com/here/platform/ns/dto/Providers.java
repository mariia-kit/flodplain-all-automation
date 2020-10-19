package com.here.platform.ns.dto;

import com.here.platform.common.config.Conf;
import com.here.platform.ns.helpers.LoggerHelper;
import com.here.platform.ns.helpers.UniqueId;
import java.util.List;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.antlr.v4.runtime.atn.RangeTransition;


@Getter
@AllArgsConstructor
public enum Providers {
    DAIMLER_EXPERIMENTAL(
            new DataProvider("daimler_experimental", "https://api.mercedes-benz.com/experimental/connectedvehicle/v1")
                    .addResources(List.of(
                            ContainerResources.ODOMETER,
                            ContainerResources.FUEL,
                            ContainerResources.CHARGE,
                            ContainerResources.TIRES,
                            ContainerResources.DOORS,
                            ContainerResources.oil
                    ))
    ),
    BMW(new DataProvider("bmw", "https://api.bmwgroup.com/otpdatadelivery/api/thirdparty/v1")
            .addResources(List.of(ContainerResources.mileage, ContainerResources.FUEL, ContainerResources.doorsstatus))),
    BMW_TEST(new DataProvider("test-bmw", Conf.ns().getRefProviderUrl() + "/bmw")
            .addResources(List.of(ContainerResources.mileage, ContainerResources.FUEL))),
    NOT_EXIST(new DataProvider("rimak", "http://www.rim.com")),
    DAIMLER_CAPITAL(new DataProvider("Daimler", "https://api.mercedes-benz.com/vehicledata/v1")),
    REFERENCE_PROVIDER(
            new DataProvider(Conf.ns().getRefProviderName(), Conf.ns().getRefProviderUrl())
                    .addResources(List.of(
                            ContainerResources.ODOMETER,
                            ContainerResources.FUEL,
                            ContainerResources.CHARGE,
                            ContainerResources.TIRES,
                            ContainerResources.DOORS,
                            ContainerResources.payasyoudrive,
                            ContainerResources.fuelstatus
                    ))
    ),
    DAIMLER_REFERENCE(new DataProvider("daimleR_experimental", Conf.ns().getRefProviderUrl())
            .addResources(List.of(
                    ContainerResources.ODOMETER,
                    ContainerResources.FUEL,
                    ContainerResources.CHARGE,
                    ContainerResources.TIRES,
                    ContainerResources.DOORS,
                    ContainerResources.oil,
                    ContainerResources.vehicles
            ))
    ),
    DAIMLER_REAL(new DataProvider("daimler", "https://api.mercedes-benz.com/vehicledata/v1")),
    REFERENCE_PROVIDER_PROD(new DataProvider("reference_provider", Conf.ns().getRefProviderUrl())
            .addResources(List.of(
                    ContainerResources.ODOMETER,
                    ContainerResources.FUEL,
                    ContainerResources.CHARGE,
                    ContainerResources.TIRES,
                    ContainerResources.DOORS,
                    ContainerResources.payasyoudrive,
                    ContainerResources.fuelstatus
            ))
    );

    private final DataProvider provider;

    public static String getDataProviderNamePrefix() {
        return "AutomatedTestProvider";
    }

    public static DataProvider generateNew() {
        DataProvider dataProvider = new DataProvider(
                getDataProviderNamePrefix() + UniqueId.getUniqueKey(),
                "http://www.here.com"
        );

        dataProvider.addResource(ContainerResources.ODOMETER);
        LoggerHelper.logStep("Generate new DataProvider:" + dataProvider.toString());
        return dataProvider;
    }

    public static Providers getByProviderId(String providerId) {
        return Stream.of(Providers.values())
                .filter(prov -> prov.getProvider().getName().equals(providerId))
                .findFirst().orElseThrow(() -> new RuntimeException("No provider with id found:" + providerId));
    }

    public String getName() {
        return this.getProvider().getName();
    }
}
