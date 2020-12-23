package com.here.platform.cm.consentStatus;

import com.here.platform.cm.BaseCMTest;
import com.here.platform.cm.controllers.ConsentStatusController;
import com.here.platform.cm.dataAdapters.ConsentInfoToConsentRequestData;
import com.here.platform.cm.enums.ConsentRequestContainer;
import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.enums.ProviderApplications;
import com.here.platform.cm.rest.model.ConsentInfo;
import com.here.platform.cm.rest.model.ConsentRequestData;
import com.here.platform.cm.steps.api.ConsentRequestSteps;
import com.here.platform.common.VinsToFile;
import com.here.platform.common.config.Conf;
import com.here.platform.dataProviders.daimler.DataSubjects;
import com.here.platform.ns.dto.User;
import com.here.platform.ns.instruments.MarketAfterCleanUp;
import java.io.File;
import org.junit.jupiter.api.extension.ExtendWith;


public class BaseConsentStatusTests extends BaseCMTest {
    protected ConsentStatusController consentStatusController = new ConsentStatusController();
}
