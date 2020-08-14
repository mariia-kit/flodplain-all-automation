package com.here.platform.ns.dto;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;


@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class VehicleDataDto {

    @NonNull
    private final String providerName;
    @NonNull
    private final String vehicleID;
    @NonNull
    private final String containerID;

    private Map<String, String> resultMap;

}
