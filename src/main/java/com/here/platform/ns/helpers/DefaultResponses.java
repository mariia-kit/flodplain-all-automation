package com.here.platform.ns.helpers;

import com.here.platform.ns.dto.Container;
import com.here.platform.ns.dto.DataProvider;
import com.here.platform.ns.dto.ProviderResource;
import io.restassured.response.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;


public class DefaultResponses {

    public static boolean isContainerPresentInList(String containerId, Response response) {
        if (response.getStatusCode() == 200) {
            ArrayList<HashMap> containerList = response.getBody().jsonPath().get();
            for (HashMap container : containerList) {
                if (container.get("id").toString().equalsIgnoreCase(containerId)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isDataProviderPresentInList(DataProvider dataProvider,
            Response response) {
        if (response.getStatusCode() == 200) {
            ArrayList<HashMap> dataProviderList = response.getBody().jsonPath().get();
            for (HashMap dataProviderMap : dataProviderList) {
                if (dataProviderMap.get("id").toString().equals(dataProvider.getId())
                        &&
                        dataProviderMap.get("url").toString()
                                .equalsIgnoreCase(dataProvider.getUrl())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isDataProviderPresentInListMP(DataProvider dataProvider,
            Response response) {
        if (response.getStatusCode() == 200) {
            ArrayList<HashMap> dataProviderList = response.getBody().jsonPath().get();
            for (HashMap dataProviderMap : dataProviderList) {
                if (dataProviderMap.get("name").toString().equals(dataProvider.getId())
                        &&
                        dataProviderMap.get("url").toString()
                                .equalsIgnoreCase(dataProvider.getUrl())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isResourceInList(ProviderResource resource,
            Response response) {
        if (response.getStatusCode() == 200) {
            ArrayList<HashMap> resList = response.getBody().jsonPath().get();
            for (HashMap res : resList) {
                if (res.get("name").toString().equals(resource.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isResponseListEmpty(Response response) {
        if (response.getStatusCode() == 200) {
            ArrayList<HashMap> entityList = response.getBody().jsonPath().get();
            return entityList.isEmpty();
        }
        return true;
    }

    public static ArrayList<HashMap> extractAsList(Response response) {
        if (response.getStatusCode() == 200) {
            ArrayList<HashMap> entityList = response.getBody().jsonPath().get();
            return entityList;
        }
        return new ArrayList<>();
    }

    public static Container extractAsContainer(Response response) {
        try {
            return response.getBody().as(Container.class);
        } catch (ClassCastException | IllegalStateException e) {
            throw new RuntimeException(
                    "No sign of Container in result body detected!" + response.asString());
        }
    }

    public static DataProvider extractAsProvider(Response response) {
        try {
            return response.getBody().as(DataProvider.class);
        } catch (ClassCastException | IllegalStateException e) {
            throw new RuntimeException(
                    "No sign of DataProvider in result body detected!" + response.asString());
        }
    }

    public static Map<String, String> extractAsContainerData(Response response) {
        Map<String, String> res = new HashMap<>();
        if (response.getStatusCode() == 200) {
            HashMap<String, HashMap> resList = response.getBody().jsonPath().get();
            return resList.entrySet().stream().collect(Collectors
                    .toMap(Entry::getKey, resMap -> resMap.getValue().get("value").toString()));
        }
        return res;
    }

    public static Map<String, String> extractAsISOContainerData(Response response) {
        Map<String, String> res = new HashMap<>();
        if (response.getStatusCode() == 200) {
            List<HashMap<String, HashMap>> resSets = response.getBody().jsonPath().getList("$");
            resSets.forEach(resObj ->
                    res.putAll(resObj.entrySet().stream().collect(Collectors
                            .toMap(Entry::getKey, resMap -> resMap.getValue().get("value").toString())))

            );
        }
        return res;
    }

    public static Container extractContainerPresentInList(String containerId, Response response) {
        if (response.getStatusCode() == 200) {
            try {
                Container[] res = response.getBody().as(Container[].class);
                for (Container cont : res) {
                    if (cont.getId().equals(containerId)) {
                        return cont;
                    }
                }
            } catch (ClassCastException | IllegalStateException e) {
                throw new RuntimeException("No sign of Container in result body detected!");
            }
        }
        return null;
    }

}
