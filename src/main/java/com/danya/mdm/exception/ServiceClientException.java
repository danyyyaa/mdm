package com.danya.mdm.exception;

import lombok.Getter;

@Getter
public class ServiceClientException extends MdmException {

    public ServiceClientException(String message) {
        super(message);
    }

    public ServiceClientException(String message, Exception e) {
        super(message, e);
    }
}
