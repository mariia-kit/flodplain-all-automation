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
    private String providerName;
    @NonNull
    private String vehicleID;
    @NonNull
    private String containerID;

    private Map<String, String> resultMap;

}
