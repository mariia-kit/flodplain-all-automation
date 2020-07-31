package com.here.platform.mp.controllers;

import com.here.platform.mp.models.CreateInvite;
import com.here.platform.mp.models.CreatedInvite;
import io.qameta.allure.Step;
import java.util.Arrays;


public class ListingsController extends BaseMPController<ListingsController> {

    private final String baseListingInvitePath = "/listings/invites";

    @Step
    public CreatedInvite inviteConsumerToListing(CreateInvite createInvite) {
        return Arrays.asList(mpClient(baseListingInvitePath)
                .body(createInvite)
                .post()
                .then().assertThat().statusCode(200)
                .extract()
                .as(CreatedInvite[].class)).get(0);
    }

    @Step
    public void acceptInvite(String inviteId) {
        mpClient(baseListingInvitePath)
                .post("{inviteId}/clicked?ignoreSameRealmsCheck", inviteId)
                .then().assertThat().statusCode(200)
                .extract();
    }

}
