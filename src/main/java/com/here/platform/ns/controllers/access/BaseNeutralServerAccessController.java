package com.here.platform.ns.controllers.access;

import com.here.platform.common.config.Conf;
import com.here.platform.ns.controllers.BaseNeutralService;
import io.restassured.specification.RequestSpecification;
import java.util.Map;


public class BaseNeutralServerAccessController<T> extends BaseNeutralService<T> {

    private String crid = null;
    private Map<String, String> queryParam = null;

    protected RequestSpecification neutralServerAccessClient(final String targetPath) {
        var baseService = neutralServerClient(targetPath, Conf.ns().getNsUrlBaseAccess());

        if (crid != null) {
            baseService.header("ConsentRequestID", crid);
        }
        if (queryParam != null) {
            queryParam.entrySet().forEach(e -> baseService.queryParam(e.getKey(), e.getValue()));
        }
        return baseService;
    }

    public T withConsentId(String campaignId) {
        crid = campaignId;
        return (T) this;
    }

    public T withQueryParam(String key, String value) {
        queryParam = Map.of("additional-fields", key, "additional-values", value);
        return (T) this;
    }

}
