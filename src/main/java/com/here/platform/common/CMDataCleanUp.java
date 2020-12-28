package com.here.platform.common;

import com.here.platform.cm.steps.api.UserAccountSteps;
import com.here.platform.dataProviders.daimler.DataSubjects;

public class CMDataCleanUp {


    public static void main(String[] args) {
        for (DataSubjects dataSubjects : DataSubjects.values()) {
            UserAccountSteps.removeVINFromDataSubject(dataSubjects);
        }
    }
}
