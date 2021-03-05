package com.here.platform.proxy.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class ProxyError {
    private int status;
    private String title;
    private String code;
    private String errorCause;
    private String action;
    private String errorTag;
}
