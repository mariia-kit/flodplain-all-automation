package com.here.platform.aaa;

import com.here.platform.dataProviders.DataSubjects;


public class HereCMBearerAuthorization {

    /**
     * Add to test @Execution(ExecutionMode.SAME_THREAD) that will use this method
     */
    public static String getCmToken(DataSubjects targetSubject) {
        return targetSubject.getBearerToken();
    }

}
