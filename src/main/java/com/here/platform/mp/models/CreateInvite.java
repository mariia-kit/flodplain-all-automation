package com.here.platform.mp.models;


import java.util.List;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class CreateInvite {

    private String
            listingHrn,
            deliveryMethod,
            callbackUrl;
    private List<String> emails;

}
