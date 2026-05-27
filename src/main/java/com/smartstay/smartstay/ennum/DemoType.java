package com.smartstay.smartstay.ennum;

import lombok.Getter;

@Getter
public enum DemoType {
    LIVE("Live"),
    ONLINE("Online");

    public final String value;

    DemoType(String value) {
        this.value = value;
    }
}
