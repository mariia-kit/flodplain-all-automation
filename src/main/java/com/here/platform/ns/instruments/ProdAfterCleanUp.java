package com.here.platform.ns.instruments;

import static com.here.platform.ns.dto.Users.PROVIDER;

import com.here.platform.ns.controllers.provider.ContainerController;
import com.here.platform.ns.dto.Container;
import com.here.platform.ns.dto.Containers;
import com.here.platform.ns.dto.Providers;
import com.here.platform.ns.helpers.CleanUpHelper;
import com.here.platform.ns.restEndPoints.NeutralServerResponseAssertion;
import com.here.platform.mp.steps.api.MarketplaceSteps;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;


@RequiredArgsConstructor
public class ProdAfterCleanUp implements AfterAllCallback {

    private final static Logger logger = Logger.getLogger(ProdAfterCleanUp.class);

    public void afterAll(ExtensionContext context) {
        logger.info("Clean up after prod tests start!");

        CleanUpHelper.getSubsList().stream().forEach(id ->
                {
                    logger.info("Clean Subs with id:" + id);
                    new MarketplaceSteps().beginCancellation(id);
                }
        );
        Map<String, String> allListings = new HashMap<>(CleanUpHelper.getListingList());
        allListings.entrySet().stream().forEach(lst ->
                {
                    logger.info("Clean Listing with hrn:" + lst.getKey());
                    new MarketplaceSteps().deleteListing(lst.getKey());
                }
        );
        CleanUpHelper.getContainersList().stream().forEach(id ->
                {
                    logger.info("Clean Containers with id:" + id);
                    Container container = Containers.generateNew(Providers.REFERENCE_PROVIDER_PROD.getName()).withId(id);
                    var response = new ContainerController()
                            .withToken(PROVIDER)
                            .deleteContainer(container);
                    new NeutralServerResponseAssertion(response)
                            .expectedCode(HttpStatus.SC_NO_CONTENT);
                }
        );

        CleanUpHelper.getListingList().clear();
        CleanUpHelper.getSubsList().clear();
        CleanUpHelper.getContainersList().clear();
    }

}
