package com.here.platform.ns.instruments;

import com.here.platform.ns.helpers.CleanUpHelper;
import com.here.platform.ns.restEndPoints.external.MarketplaceManageListingCall;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;


@RequiredArgsConstructor
public class MarketAfterCleanUp implements AfterAllCallback {

    private final static Logger logger = Logger.getLogger(MarketAfterCleanUp.class);

    public void afterAll(ExtensionContext context) {
        logger.info("Clean up after mp test start!");

        CleanUpHelper.getSubsList().forEach(id ->
                {
                    logger.info("Clean Subs with id:" + id);
                    new MarketplaceManageListingCall().beginCancellation(id);
                }
        );
        Map<String, String> allListings = new HashMap<>(CleanUpHelper.getListingList());
        allListings.forEach((key, value) -> {
            logger.info("Clean Listing with hrn:" + key);
            new MarketplaceManageListingCall().deleteListing(key);
        });

        CleanUpHelper.getListingList().clear();
        CleanUpHelper.getSubsList().clear();
    }

}
