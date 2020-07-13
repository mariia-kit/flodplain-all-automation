package com.here.platform.ns.restEndPoints.external;

import static com.here.platform.ns.dto.Users.MP_CONSUMER;

import com.here.platform.ns.helpers.resthelper.RestHelper;
import com.here.platform.ns.restEndPoints.BaseRestControllerNS;
import com.here.platform.ns.utils.NS_Config;
import io.restassured.response.Response;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.function.Supplier;


public class MarketplaceCMAddVinsCall extends
        BaseRestControllerNS<MarketplaceCMAddVinsCall> {

    private String vin;
    private String subsId;

    public MarketplaceCMAddVinsCall(String subsId, String vin) {
        callMessage = String
                .format("Perform MP call to add vin numbers to ConsentRequest");
        setDefaultUser(MP_CONSUMER);
        endpointUrl =
                NS_Config.URL_EXTERNAL_MARKETPLACE + "/consent/subscriptions/" + subsId + "/request";
        this.vin = vin;
        this.subsId = subsId;
    }

    @Override
    public Supplier<Response> defineCall() {
        return () -> {
            File file = new File("VIN_subs_" + subsId + ".csv");

            try (FileWriter writer = new FileWriter(file)) {
                writer.write(vin);
            } catch (IOException ex) {
                throw new RuntimeException("Error writing vins to file" + file.getAbsolutePath());
            }

            Response resp = RestHelper.putFile("Add vin number to Consent", getEndpointUrl(), getToken(), file, "text/csv", "x-mp-addvins-" + subsId);
            file.delete();
            return resp;
        };
    }

}
