package com.here.platform.proxy.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
@Getter
public enum ProxyProviderResources {
    REFERENCE_RESOURCE(new ProxyProviderResource(75L,"Auto-testing-reference","/proxy/data",
            "hrn:here-dev:extsvc::olp-sit-mrkt-p2:b2fabca0-reference_data_provider_ost_solo_experiments_com-proxy_data")),
    ACCU_MAP_GSIR(new ProxyProviderResource("Accuweather Maps - Radar","/maps/v1/radar/globalSIR/quadkey/213.png")),
    ACCU_MAP_GIR(new ProxyProviderResource("Accuweather Maps - Satellite","/maps/v1/satellite/globalIR/quadkey/213.png")),
    ACCU_MAP_FSIR(new ProxyProviderResource("Accuweather Maps - Future Radar","maps/v1/radar/futureSIR/quadkey/213.png"));


    private ProxyProviderResource resource;
}
