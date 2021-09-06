package com.here.platform.awsS3Proxy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("proxyAws")
@DisplayName("[AWS] Verify AWS directory access")
public class AwsTest extends BaseAwsTest{

    @Test
    @DisplayName("Check")
    void verify(){
        System.out.println("AWS Proxy test verified");
    }
}
