package com.here.platform.common.config;

import lombok.Data;


@Data
@YamlConfUrl(propertyName = "CREDENTIAL_FILE_MP")
public class MpConfig {

    private String marketplaceUrl;
    private String marketplaceUiBaseUrl;
    private String marketplaceUiUrl;
    private String marketplaceCallbackUrl;

}
