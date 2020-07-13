package com.here.platform.cm.enums;

import static com.here.platform.cm.enums.ConsentRequestContainers.RealDaimlerApplication.CLIENT_ID;
import static com.here.platform.cm.enums.ConsentRequestContainers.RealDaimlerApplication.CLIENT_SECRET;

import com.here.platform.common.EnumByEnv;
import java.util.List;
import lombok.AllArgsConstructor;


/**
 * Possible to use as a container name for provider's applications
 */
@AllArgsConstructor
public enum ConsentRequestContainers {

    PAY_AS_YOU_DRIVE(
            "payasyoudrive", "Pay as you drive insurance", "Pay as you drive insurance",
            "mb:vehicle:mbdata:payasyoudrive",
            CLIENT_ID, CLIENT_SECRET,
            List.of("odometer"),
            MPProviders.DAIMLER
    ),
    VEHICLE_STATUS(
            "vehiclestatus", "Vehicle status", "Vehicle status",
            "mb:vehicle:mbdata:vehiclestatus",
            CLIENT_ID, CLIENT_SECRET,
            List.of("odometer"),
            MPProviders.DAIMLER
    ),
    ELECTRIC_VEHICLE_STATUS(
            "electricvehicle", "Electric vehicle status", "Electric vehicle status",
            "mb:vehicle:mbdata:evstatus",
            CLIENT_ID, CLIENT_SECRET,
            List.of("odometer"),
            MPProviders.DAIMLER
    ),
    FUEL_STATUS(
            "fuelstatus", "Fuel status", "Fuel status",
            "mb:vehicle:mbdata:fuelstatus",
            CLIENT_ID, CLIENT_SECRET,
            List.of("odometer"),
            MPProviders.DAIMLER
    ),
    VEHICLE_LOCK_STATUS(
            "vehiclelockstatus", "Vehicle lock status", "Vehicle lock status",
            "mb:vehicle:mbdata:vehiclelock",
            CLIENT_ID, CLIENT_SECRET,
            List.of("odometer"),
            MPProviders.DAIMLER
    ),

    CONNECTED_VEHICLE(
            "connectedvehicle", "connectedvehicle",
            "This experimental product allows you to get access to important telematics data, status info and vehicle functions from virtual Mercedes–Benz cars",
            "mb:user:pool:reader mb:vehicle:status:general",
            EnumByEnv.get(ConnectedVehicleCredentials.class).clientId,
            EnumByEnv.get(ConnectedVehicleCredentials.class).clientSecret,
            List.of("odometer", "location"),
            MPProviders.DAIMLER_EXPERIMENTAL
    );
    //TODO implement reusing of provider from container for consent request creation and onboarding

    public final String id, name, containerDescription, scopeValue, clientId, clientSecret;
    public List<String> resources;
    public MPProviders provider;

    public static ConsentRequestContainers getRandom() {
        return values()[(int) (Math.random() * values().length - 1)]; //except CONNECTED_VEHICLE
    }

    @AllArgsConstructor
    public enum ConnectedVehicleCredentials {
        LOCAL("7ad8cbff-d257-4182-b41f-2a4afd013e47", "9d9b8e0a-04b3-4a78-aa8d-8143ccd0e6f3"),
        DEV("7ad8cbff-d257-4182-b41f-2a4afd013e47", "9d9b8e0a-04b3-4a78-aa8d-8143ccd0e6f3"),
        SIT("837df42f-500f-4b3d-be3d-c57e11b61f45", "240c3498-d266-4b30-a837-7d7220987cef"),
        PROD("93dc654d-eabe-4437-a3ae-fdb0ce32b58b", "3b789e8a-e472-4cd5-875a-f4c773f4bc4d");
        String clientId, clientSecret;
    }

    public interface RealDaimlerApplication {

        String CLIENT_ID = "15998dfd-a3e2-4dae-94bb-745bdf3351af";
        String CLIENT_SECRET = "08c7e4c3-0fed-485f-b634-6d95093ec689";

    }

}
