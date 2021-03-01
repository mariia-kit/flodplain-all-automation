package com.here.platform.proxy;

import com.here.platform.common.annotations.CMFeatures.ZephyrComponent;
import com.here.platform.proxy.helper.ProxyRemoveExtension;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;


@Tag("proxy")
@ZephyrComponent("Proxy")
@Execution(ExecutionMode.CONCURRENT)
public class BaseProxyTests {
    @RegisterExtension
    ProxyRemoveExtension proxyRemoveExtension = new ProxyRemoveExtension();
}
