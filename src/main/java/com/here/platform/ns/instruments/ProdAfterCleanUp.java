package com.here.platform.ns.instruments;

import com.here.platform.ns.dto.Providers;
import com.here.platform.ns.helpers.CleanUpHelper;
import com.here.platform.ns.restEndPoints.external.MarketplaceManageListingCall;
import com.here.platform.ns.restEndPoints.provider.container_info.DeleteContainerCall;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
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
                    new MarketplaceManageListingCall().beginCancellation(id);
                }
        );
        Map<String, String> allListings = new HashMap<>(CleanUpHelper.getListingList());
        allListings.entrySet().stream().forEach(lst ->
                {
                    logger.info("Clean Listing with hrn:" + lst.getKey());
                    new MarketplaceManageListingCall().deleteListing(lst.getKey());
                }
        );
        CleanUpHelper.getContainersList().stream().forEach(id ->
                {
                    logger.info("Clean Containers with id:" + id);
                    new DeleteContainerCall(id, Providers.DAIMLER_REFERENCE.getName()).call();
                }
        );

        CleanUpHelper.getListingList().clear();
        CleanUpHelper.getSubsList().clear();
        CleanUpHelper.getContainersList().clear();
    }

}
