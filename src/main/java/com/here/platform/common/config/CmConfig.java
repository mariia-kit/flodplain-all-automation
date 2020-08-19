package com.here.platform.common.config;

import lombok.Data;


@Data
@YamlConfUrl(configUrl = "{env}/cm.yaml")
public class CmConfig {

    private String serviceUrl;

    private String consentPageUrl;
}
