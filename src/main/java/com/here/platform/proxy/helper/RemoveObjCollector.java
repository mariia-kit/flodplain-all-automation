package com.here.platform.proxy.helper;

import io.qameta.allure.Allure;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.UtilityClass;


@UtilityClass
public class RemoveObjCollector {
    private static Map<String, List<Long>> proxyProviders = new ConcurrentHashMap<>();
    private static Map<String, List<PairIdValue>> proxyResource = new ConcurrentHashMap<>();
    private static Map<String, List<String>> proxyHrn = new ConcurrentHashMap<>();

    public void addProxyProvider(Long id) {
        String testId = Allure.getLifecycle().getCurrentTestCase()
                .orElseThrow(() -> new RuntimeException("Test case not detected while adding id" + id));
        if (!proxyProviders.containsKey(testId)) {
            proxyProviders.put(testId, new ArrayList<>());
        }
        proxyProviders.get(testId).add(id);
    }

    public void addResourceToProxyProvider(String providerId, Long... resIds) {
        String testId = Allure.getLifecycle().getCurrentTestCase().get();
        if (!proxyResource.containsKey(testId)) {
            proxyResource.put(testId, new ArrayList<>());
        }
        for (Long resId: resIds) {
            proxyResource.get(testId).add(new PairIdValue(providerId, resId));
        };
    }

    public void removeResourceFromProxyProvider(Long... resIds) {
        String testId = Allure.getLifecycle().getCurrentTestCase().get();
        if (!proxyResource.containsKey(testId)) {
            proxyResource.put(testId, new ArrayList<>());
        }
        for (Long resId: resIds) {
            proxyResource.put(testId, proxyResource.get(testId).stream()
                    .filter(r -> !r.getId().equals(resId))
                    .collect(Collectors.toList()));
        };
    }

    public void addProxyResHrn(String hrn) {
        String testId = Allure.getLifecycle().getCurrentTestCase()
                .orElseThrow(() -> new RuntimeException("Test case not detected while adding hrn" + hrn));
        if (!proxyHrn.containsKey(testId)) {
            proxyHrn.put(testId, new ArrayList<>());
        }
        proxyHrn.get(testId).add(hrn);
    }

    public List<Long> getAllProxyProviders(String testId) {
        return proxyProviders.getOrDefault(testId, new ArrayList<>());
    }

    public List<PairIdValue> getAllProxyProvidersWithResources(String testId) {
        return proxyResource.getOrDefault(testId, new ArrayList<>());
    }

    public List<String> getAllProxyResHrn(String testId) {
        return proxyHrn.getOrDefault(testId, new ArrayList<>());
    }

    @Data
    @AllArgsConstructor
    public static class PairIdValue {
        private String key;
        private Long id;
    }
}
