package com.here.platform.cm.dataAdapters;

import com.here.platform.cm.rest.model.ConsentInfo;
import com.here.platform.cm.rest.model.ConsentRequestData;


public class ConsentRequestToConsentInfo {

    private final ConsentInfo consentInfo;

    public ConsentRequestToConsentInfo(String crid, ConsentRequestData consentRequestData) {
        this.consentInfo = new ConsentInfo()
                .title(consentRequestData.getTitle())
                .purpose(consentRequestData.getPurpose())
                .containerName(consentRequestData.getContainerName())
                .consentRequestId(crid);
    }

    public ConsentInfo consentInfo() {
        return consentInfo;
    }

}
