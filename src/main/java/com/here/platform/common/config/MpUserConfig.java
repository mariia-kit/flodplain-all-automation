package com.here.platform.common.config;

import com.here.platform.ns.dto.User;
import lombok.Data;


@Data
@YamlConfUrl(propertyName = "CREDENTIAL_FILE_MP_USER")
public class MpUserConfig {

    private User mpConsumer;
    private User mpProvider;

    private String consumerName;

}
