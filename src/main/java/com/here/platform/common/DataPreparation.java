package com.here.platform.common;

import com.here.platform.dataProviders.DataSubjects;


public class DataPreparation {

    public static void main(String[] args) {
        for (DataSubjects dataSubjects : DataSubjects.values()) {
            dataSubjects.generateBearerToken();
        }
    }

}