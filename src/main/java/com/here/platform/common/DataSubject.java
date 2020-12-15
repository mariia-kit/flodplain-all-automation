package com.here.platform.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataSubject {

    public String
            email,
            pass,
            vin;

}
