package com.here.platform.ns.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum ContainerResources {
    ODOMETER(new ProviderResource("odometer")),
    LOCATION(new ProviderResource("location")),
    FUEL(new ProviderResource("fuel")),
    CHARGE(new ProviderResource("stateofcharge")),
    TIRES(new ProviderResource("tires")),
    DOORS(new ProviderResource("doors")),

    ///vehicles/{vehicleId}/containers/payasyoudrive
    odo(new ProviderResource("odo")),
    ///vehicles/{vehicleId}/containers/electricvehicle
    rangeelectric(new ProviderResource("rangeelectric")),
    soc(new ProviderResource("soc")),

    //vehicles​/{vehicleId}​/containers​/fuelstatus
    rangeliquid(new ProviderResource("rangeliquid")),
    tankLevelpercent(new ProviderResource("tanklevelpercent")),

    //vehicles/{vehicleId}/containers/vehiclelockstatus
    doorlockstatusvehicle(new ProviderResource("doorlockstatusvehicle")),
    doorlockstatusdecklid(new ProviderResource("doorlockstatusdecklid")),
    doorlockstatusgas(new ProviderResource("doorlockstatusgas")),
    positionHeading(new ProviderResource("positionHeading")),

    //vehicles​/{vehicleId}​/containers​/vehiclestatus
    rooftopstatus(new ProviderResource("rooftopstatus")),
    decklidstatus(new ProviderResource("decklidstatus")),
    lightswitchposition(new ProviderResource("lightswitchposition")),
    interiorLightsFront(new ProviderResource("interiorLightsFront")),
    interiorLightsRear(new ProviderResource("interiorLightsRear")),
    readingLampFrontLeft(new ProviderResource("readingLampFrontLeft")),
    readingLampFrontRight(new ProviderResource("readingLampFrontRight")),
    windowstatusfrontleft(new ProviderResource("windowstatusfrontleft")),
    windowstatusfrontright(new ProviderResource("windowstatusfrontright")),
    windowstatusrearleft(new ProviderResource("windowstatusrearleft")),
    windowstatusrearright(new ProviderResource("windowstatusrearright")),
    sunroofstatus(new ProviderResource("sunroofstatus")),
    doorstatusfrontleft(new ProviderResource("doorstatusfrontleft")),
    doorstatusfrontright(new ProviderResource("doorstatusfrontright")),
    doorstatusrearleft(new ProviderResource("doorstatusrearleft")),
    doorstatusrearright(new ProviderResource("doorstatusrearright")),

    payasyoudrive(new ProviderResource("payasyoudrive")),
    fuelstatus(new ProviderResource("fuelstatus")),
    oil(new ProviderResource("oil")),
    vehicles(new ProviderResource("vehicles")),

    mileage(new ProviderResource("mileage"));

    //TODO: add bmw resource if necessary
    //Electric Vehicle
    //bmwcardata_batteryVoltage,bmwcardata_naviInformationRemainingRange
    //Fuel Status
    //bmwcardata_kombiCurrentRemainingRangeFuel
    //bmwcardata_remainingFuel
    //Pay As You Drive (PAYD)
    //bmwcardata_mileage
    //Vehicle Lock Status
    //bmwcardata_vehicleStatusDoors
    //bmwcardata_trunkState
    //bmwcardata_hoodState
    //bmwcardata_sunroofState
    //bmwcardata_heading
    //Vehicle Status
    //bmwcardata_sunroofPosition
    //bmwcardata_sunroofState
    //bmwcardata_trunkState
    //bmwcardata_vehicleStatusLightstatus
    //bmwcardata_windowDriverFront
    //bmwcardata_windowDriverRear
    //bmwcardata_windowPassengerFront
    //bmwcardata_windowPassengerRear
    //bmwcardata_doorDriverFront
    //bmwcardata_doorDriverRear
    //bmwcardata_doorLockState
    //bmwcardata_doorPassengerFront
    //bmwcardata_doorPassengerRear

    private final ProviderResource resource;

    public String getName() {
        return this.resource.getName();
    }
}
