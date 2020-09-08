package com.here.platform.ns.dto;

import com.here.platform.ns.helpers.ContainerResourcesHelper;
import com.here.platform.ns.helpers.LoggerHelper;
import com.here.platform.ns.helpers.UniqueId;
import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
@Getter
public enum Containers {
    DAIMLER_EXPERIMENTAL_ODOMETER(
            new Container("odometer", "odometer",
                    Providers.DAIMLER_EXPERIMENTAL.getName(),
                    "Provides odometer specific information.",
                    ContainerResources.ODOMETER.getResource().getName(), true,
                    "mb:user:pool:reader mb:vehicle:status:general")
    ),
    DAIMLER_EXPERIMENTAL_FUEL(
            new Container("fuel", "fuel",
                    Providers.DAIMLER_EXPERIMENTAL.getName(),
                    "Provides fuel specific information.",
                    ContainerResources.FUEL.getResource().getName(), true,
                    "mb:user:pool:reader mb:vehicle:status:general")
    ),
    DAIMLER_EXPERIMENTAL_TIRES(
            new Container("tires", "tires",
                    Providers.DAIMLER_EXPERIMENTAL.getName(),
                    "Provides information about the tire pressure.",
                    ContainerResources.TIRES.getResource().getName(), true,
                    "mb:user:pool:reader mb:vehicle:status:general")
    ),
    DAIMLER_EXPERIMENTAL_DOORS(
            new Container("doors", "doors",
                    Providers.DAIMLER_EXPERIMENTAL.getName(),
                    "Provides information about the doors status.",
                    ContainerResources.DOORS.getResource().getName(), true,
                    "mb:user:pool:reader mb:vehicle:status:general")
    ),
    DAIMLER_EXPERIMENTAL_LOCATION(
            new Container("location", "location",
                    Providers.DAIMLER_EXPERIMENTAL.getName(),
                    "Provides location information about vehicle.",
                    ContainerResources.LOCATION.getResource().getName(), true,
                    "mb:user:pool:reader mb:vehicle:status:general")
    ),
    DAIMLER_EXPERIMENTAL_CHARGE(
            new Container("stateofcharge", "stateofcharge",
                    Providers.DAIMLER_EXPERIMENTAL.getName(),
                    "Provides charge status of the battery pack.",
                    ContainerResources.CHARGE.getResource().getName(), true,
                    "mb:user:pool:reader mb:vehicle:status:general")
    ),
    DAIMLER_EXPERIMENTAL_CONNECTED_VEHICLE(
            new Container("connectedvehicle", "connectedvehicle",
                    Providers.DAIMLER_EXPERIMENTAL.getName(),
                    "This experimental product allows you to get access to important telematics data, status info and vehicle functions from virtual Mercedes–Benz cars",
                    String.join(",",
                            "odometer", "location"
                    ), true,
                    "mb:user:pool:reader mb:vehicle:status:general")
    ),
    DAIMLER_PAYASYOUDRIVE(
            new Container("payasyoudrive", "payasyoudrive",
                    Providers.DAIMLER_REAL.getName(),
                    "Pay As You Drive Insurance service for view all Pay As You Drive Insurance vehicle data points",
                    ContainerResources.odo.getResource().getName(),
                    true, "mb:vehicle:mbdata:payasyoudrive")
    ),
    DAIMLER_ELECTRICVEHICLE(
            new Container("electricvehicle", "electricvehicle",
                    Providers.DAIMLER_REAL.getName(),
                    "Electric Vehicle Status service for view all electric status vehicle data points",
                    String.join(",",
                            ContainerResources.soc.getResource().getName(),
                            ContainerResources.rangeelectric.getResource().getName()
                    ),
                    true, "mb:vehicle:mbdata:electricvehicle")
    ),
    DAIMLER_FUEALSTATUS(
            new Container("fuelstatus", "fuelstatus",
                    Providers.DAIMLER_REAL.getName(),
                    "Fuel Status service for view all Fuel Status vehicle data points",
                    String.join(",",
                            ContainerResources.rangeliquid.getResource().getName(),
                            ContainerResources.tankLevelpercent.getResource().getName()
                    ),
                    true, "mb:vehicle:mbdata:fuelstatus")
    ),
    DAIMLER_VEHICLELOCK(
            new Container("vehiclelockstatus", "vehiclelockstatus",
                    Providers.DAIMLER_REAL.getName(),
                    "Vehicle Status service for view all Vehicle Status vehicle data points",
                    String.join(",",
                            ContainerResources.doorlockstatusdecklid.getResource().getName(),
                            ContainerResources.doorlockstatusvehicle.getResource().getName(),
                            ContainerResources.doorlockstatusgas.getResource().getName(),
                            ContainerResources.positionHeading.getResource().getName()
                    ),
                    true, "mb:vehicle:mbdata:vehiclelockstatus")
    ),
    DAIMLER_VEHICLESTATUS(
            new Container("vehiclestatus", "vehiclestatus",
                    Providers.DAIMLER_REAL.getName(),
                    "Vehicle Status service for view all Vehicle Status vehicle data points",
                    String.join(",",
                            ContainerResources.decklidstatus.getResource().getName(),
                            ContainerResources.doorstatusfrontleft.getResource().getName(),
                            ContainerResources.doorstatusfrontright.getResource().getName(),
                            ContainerResources.doorstatusrearleft.getResource().getName(),
                            ContainerResources.doorstatusrearright.getResource().getName(),
                            ContainerResources.interiorLightsFront.getResource().getName(),
                            ContainerResources.interiorLightsRear.getResource().getName(),
                            ContainerResources.lightswitchposition.getResource().getName(),
                            ContainerResources.readingLampFrontLeft.getResource().getName(),
                            ContainerResources.readingLampFrontRight.getResource().getName(),
                            ContainerResources.rooftopstatus.getResource().getName(),
                            ContainerResources.sunroofstatus.getResource().getName(),
                            ContainerResources.windowstatusfrontleft.getResource().getName(),
                            ContainerResources.windowstatusfrontright.getResource().getName(),
                            ContainerResources.windowstatusrearleft.getResource().getName(),
                            ContainerResources.windowstatusrearright.getResource().getName()
                    ),
                    true, "mb:vehicle:mbdata:vehiclestatus")
    ),
    REF_DAIMLER_ODOMETER(
            new Container(
                    "odometer", "odometer", Providers.DAIMLER_REFERENCE.getName(),
                    "Automated Test Container", "odometer", true, null
            )
    ),
    REF_DAIMLER_FUELSTATUS(
            new Container(
                    "fuel", "fuel", Providers.DAIMLER_REFERENCE.getName(),
                    "Automated Test Container", "fuel", true, null
            )
    ),
    REF_DAIMLER_CHARGE(
            new Container(
                    "stateofcharge", "stateofcharge", Providers.DAIMLER_REFERENCE.getName(),
                    "Automated Test Container", "stateofcharge", true, null
            )
    ),
    BMW_TEST_1(
            new Container(
                    "payasyoudrive", "HERE Test Container 1", Providers.BMW.getName(),
                    "This test container to be used for testing HERE Neutral Server integration with BMW CarData services",
                    "mileage",
                    true, ""
            )
    ),
    BMW_MILEAGE(
            new Container("payasyoudrive", "bmwcardata_mileage", Providers.BMW_TEST.getName(),
                    "Vehicle mileage",
                    "mileage",
                    true, "")
    );

    private final Container container;

    public static String getContainerNamePrefix() {
        return "automatedtestcontainer";
    }

    public static Container generateNew(String providerName) {
        String id = getContainerNamePrefix() + UniqueId.getUniqueKey();
        Container container = new Container(
                id,
                id,
                providerName,
                "Automated Test Container",
                ContainerResourcesHelper.REGULAR_RESOURCE,
                true,
                null);
        container.setScope(container.generateScope());
        LoggerHelper.logStep("Generate new Container:" + container.toString());
        return container;
    }

    public static Container generateNew(DataProvider provider) {
        return generateNew(provider.getName());
    }

}
