package com.here.platform.proxy.dto;

import com.here.platform.common.config.Conf;
import com.here.platform.ns.helpers.UniqueId;
//import com.here.platform.proxy.dto.ProxyProvider.CredentialsAuthMethod;
import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
@Getter
public enum ProxyProviderResourceEnum {
    REFERENCE_RESOURCE(new ProxyProviderResource(75L,"Auto-testing-reference","proxy/data",
            "hrn:here-dev:extsvc::olp-sit-mrkt-p2:b2fabca0-reference_data_provider_ost_solo_experiments_com-proxy_data")),
    ACCU_MAP_GSIR(new ProxyProviderResource("Accuweather Maps - Radar","/maps/v1/radar/globalSIR/quadkey/213.png")),
    ACCU_MAP_GIR(new ProxyProviderResource("Accuweather Maps - Satellite","/maps/v1/satellite/globalIR/quadkey/213.png")),
    ACCU_MAP_FSIR(new ProxyProviderResource("Accuweather Maps - Future Radar","/maps/v1/radar/futureSIR/quadkey/213.png")),
    ACCU_ALLERTS(new ProxyProviderResource("Accuweather Alerts","/alerts/v1/334907")),
    ACCU_FORECASTS(new ProxyProviderResource("Accuweather Forecasts","/airquality/v2/forecasts/hourly/12hour/201655"));

    private ProxyProviderResource resource;

    public static String getResourceNamePrefix() {
        return "Auto-Resource";
    }

    public static ProxyProviderResource generate() {
        String id = UniqueId.getUniqueKey();
        return new ProxyProviderResource(
                getResourceNamePrefix() + id,
                "proxy/data/d" + id);
    }

    public static ProxyProviderResource generateGenericPath() {
        String id = UniqueId.getUniqueKey();
        return new ProxyProviderResource (
                getResourceNamePrefix() + id,
                "proxy/data/*" + id);
    }

    public static ProxyProviderResource generateResourceWithSlash() {
        String id = UniqueId.getUniqueKey();
        return new ProxyProviderResource (
                getResourceNamePrefix() + id,
                "/proxy/data" + id);
    }

    public static ProxyProviderResource generateAws() {
        String id = UniqueId.getUniqueKey();
        return new ProxyProviderResource(
                getResourceNamePrefix() + id,
                "dir/subdir" + id + "/");
    }

    public static ProxyProviderResource generateAwsExistingSubdirectory() {
        String id = UniqueId.getUniqueKey();
        return new ProxyProviderResource(
                getResourceNamePrefix() + id,
                "dir_1/subdir_5/");
    }
}
