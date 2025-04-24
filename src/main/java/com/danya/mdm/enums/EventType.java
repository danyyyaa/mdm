package com.danya.mdm.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EventType {

    CHANGE_PHONE("change_phone");

    private final String value;
}
