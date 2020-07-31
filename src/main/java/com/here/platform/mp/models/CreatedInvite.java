package com.here.platform.mp.models;

import lombok.Data;


@Data
public class CreatedInvite {

    private String
            id,
            providerUserId,
            providerUserRealm,
            listingHrn,
            deliveryMethod,
            email,
            callbackUrl,
            dateCreated;
    private boolean emailSuccessfullySent;

}