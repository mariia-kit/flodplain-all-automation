package com.here.platform.ns.dto;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;


@Getter
public class Vehicle {

    public static final String validVehicleId = "2AD190A6AD057824E";
    public static final String validVehicleIdLong = "2AD190A6AD057824ED";
    public static final String validRefVehicleId = "857903401504142700";
    public static final String invalidVehicleId = "WDB123456ZZZ22222";
    public static final String experimentalVehicleId = "1HMDF9036HA5D3EFE";

    public static final Map<String, String> odometerResource = new HashMap<String, String>() {{
        put("odometer", "3005");
        put("distancesincereset", "1234");
        put("distancesincestart", "276");
    }};

    public static final Map<String, String> chargeResource = new HashMap<String, String>() {{
        put("stateofcharge", "0.8");
    }};

    public static final Map<String, String> doorsResource = new HashMap<String, String>() {{
        put("doorstatusfrontleft", "CLOSED");
        put("doorlockstatusfrontleft", "LOCKED");
        put("doorstatusfrontright", "CLOSED");
        put("doorlockstatusfrontright", "UNLOCKED");
        put("doorstatusrearright", "CLOSED");
        put("doorlockstatusrearright", "UNLOCKED");
        put("doorstatusrearleft", "CLOSED");
        put("doorlockstatusrearleft", "UNLOCKED");
        put("doorlockstatusdecklid", "UNLOCKED");
        put("doorlockstatusgas", "UNLOCKED");
        put("doorlockstatusvehicle", "UNLOCKED");
    }};

    public static final Map<String, String> locationResource = new HashMap<String, String>() {{
        put("longitude", "13.381815");
        put("latitude", "52.516506");
        put("heading", "52.520008");
    }};

    public static final Map<String, String> fuelResource = new HashMap<String, String>() {{
        put("rangeliquid", "1648");
        put("tanklevelpercent", "84");
    }};

    public static final Map<String, String> tiresResource = new HashMap<String, String>() {{
        put("tirepressurefrontleft", "230");
        put("tirepressurefrontright", "230");
        put("tirepressurerearright", "230");
        put("tirepressurerearleft", "230");
    }};

    public static final Map<String, String> empty = new HashMap<>();

    public static Map<String, String> getResourceMap(Map<String, String> resourcesSet, String resourceName) {
        return new HashMap<String, String>() {{
            put(resourceName, resourcesSet.get(resourceName));
        }};
    }

}
