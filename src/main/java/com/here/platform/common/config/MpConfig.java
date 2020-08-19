package com.here.platform.common.config;

import lombok.Data;


@Data
@YamlConfUrl(configUrl = "{env}/mp.yaml")
public class MpConfig {

    private String marketplaceUrl;
    private String marketplaceUiBaseUrl;
    private String marketplaceUiUrl;
    private String marketplaceCallbackUrl;

}
