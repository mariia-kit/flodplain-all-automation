package com.here.platform.common.config;

import com.here.platform.common.DaimlerApp;
import com.here.platform.common.DataSubject;
import lombok.Data;


@Data
@YamlConfUrl(configUrl = "{env}/cm-users.yaml")
public class CmUserConfig {

    private DaimlerApp daimlerApp;

    private DataSubject dataSubj1;
    private DataSubject dataSubj2;
    private DataSubject dataSubj3;
    private DataSubject dataSubj4;
    private DataSubject dataSubj5;
    private DataSubject dataSubj6;
    private DataSubject dataSubj7;
    private DataSubject dataSubj8;
    private DataSubject dataSubj9;
    private DataSubject dataSubj10;
    private DataSubject dataSubj11;
    private DataSubject dataSubj12;
    private DataSubject dataSubj13;

}
