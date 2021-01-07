package com.here.platform.ns.helpers;

import com.here.platform.ns.restEndPoints.external.ConsentApp;
import com.here.platform.ns.restEndPoints.external.ConsentVin;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;


public class CleanUpHelper {

    private static final Map<String, String>
            listingList = new HashMap<>(),
            artificialPolicy = new HashMap<>();
    private static final List<String>
            subsList = new CopyOnWriteArrayList<>(),
            containersList = new CopyOnWriteArrayList<>(),
            consentIdsList = new CopyOnWriteArrayList<>();
    private static final List<ConsentVin> consentVinsList = new CopyOnWriteArrayList<>();
    private static final List<ConsentApp> consentAppsList = new CopyOnWriteArrayList<>();


    private CleanUpHelper() {

    }

    public static Map<String, String> getListingList() {
        return listingList;
    }

    public static List<String> getSubsList() {
        return subsList;
    }

    public static Map<String, String> getArtificialPolicy() {
        return artificialPolicy;
    }

    public static List<String> getContainersList() {
        return containersList;
    }

    public static List<String> getConsentIdsList() {
        return consentIdsList;
    }

    public static List<ConsentVin> getConsentVinsList() {
        return consentVinsList;
    }

    public static List<ConsentApp> getConsentAppsList() {
        return consentAppsList;
    }

    public static void addToAppsList(String providerId, String containerId) {
        ConsentApp app = new ConsentApp(providerId, containerId);
        consentAppsList.add(app);
    }

    public static void addToVinsList(String consentId, String vin) {
        ConsentVin consentVin = new ConsentVin(consentId, vin);
        consentVinsList.add(consentVin);
    }


}
