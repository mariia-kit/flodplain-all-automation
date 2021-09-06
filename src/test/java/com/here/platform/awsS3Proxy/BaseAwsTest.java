package com.here.platform.awsS3Proxy;

import com.here.platform.common.annotations.CMFeatures.ZephyrComponent;
import com.here.platform.proxy.helper.ProxyRemoveExtension;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@Tag("proxyAws")
@ZephyrComponent("Proxy")
@Execution(ExecutionMode.CONCURRENT)
public class BaseAwsTest {
        @RegisterExtension
        ProxyRemoveExtension proxyRemoveExtension = new ProxyRemoveExtension();
    }
