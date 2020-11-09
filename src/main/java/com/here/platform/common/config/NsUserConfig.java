package com.here.platform.common.config;

import com.here.platform.common.DataSubject;
import com.here.platform.common.config.dto.HereApplication;
import com.here.platform.ns.dto.User;
import lombok.Data;


@Data
@YamlConfUrl(configUrl = "{env}/ns-users.yaml")
public class NsUserConfig {

    private User consumer;
    private User provider;

    private User nonConsumerManager;

    private String consumerGroupId;
    private String providerGroupId;
    private String providerPolicyId;

    private HereApplication consumerApp;
    private HereApplication aaService;

    private DataSubject daimlerUser;
    private DataSubject hereUser;

}
