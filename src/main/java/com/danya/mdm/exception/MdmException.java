package com.danya.mdm.exception;

public class MdmException extends RuntimeException {

    public MdmException(String message) {
        super(message);
    }

    public MdmException(String message, Throwable cause) {
        super(message, cause);
    }
}
