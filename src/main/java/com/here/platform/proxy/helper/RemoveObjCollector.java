package com.here.platform.proxy.helper;

import com.here.platform.cm.steps.remove.PairValue;
import io.qameta.allure.Allure;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;


@UtilityClass
public class RemoveObjCollector {
    private static Map<String, List<String>> proxyProviders = new ConcurrentHashMap<>();
    private static Map<String, List<PairValue>> proxyResource = new ConcurrentHashMap<>();

    public void addProxyProvider(String id) {
        String testId = Allure.getLifecycle().getCurrentTestCase()
                .orElseThrow(() -> new RuntimeException("Test case not detected while adding id" + id));
        if (!proxyProviders.containsKey(testId)) {
            proxyProviders.put(testId, new ArrayList<>());
        }
        proxyProviders.get(testId).add(id);
    }

    public void addResourceToProxyProvider(String providerId, String... resIds) {
        String testId = Allure.getLifecycle().getCurrentTestCase().get();
        if (!proxyResource.containsKey(testId)) {
            proxyResource.put(testId, new ArrayList<>());
        }
        for (String resId: resIds) {
            proxyResource.get(testId).add(new PairValue(providerId, resId));
        };
    }

    public void removeResourceFromProxyProvider(String... resIds) {
        String testId = Allure.getLifecycle().getCurrentTestCase().get();
        if (!proxyResource.containsKey(testId)) {
            proxyResource.put(testId, new ArrayList<>());
        }
        for (String resId: resIds) {
            proxyResource.put(testId, proxyResource.get(testId).stream()
                    .filter(r -> !r.getValue().equals(resId))
                    .collect(Collectors.toList()));
        };
    }

    public List<String> getAllProxyProviders(String testId) {
        return proxyProviders.getOrDefault(testId, new ArrayList<>());
    }

    public List<PairValue> getAllProxyProvidersWithResources(String testId) {
        return proxyResource.getOrDefault(testId, new ArrayList<>());
    }
}
