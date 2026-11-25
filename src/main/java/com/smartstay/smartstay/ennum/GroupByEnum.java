package com.smartstay.smartstay.ennum;

import lombok.Getter;

@Getter
public enum GroupByEnum {

    FLOOR_ROOM_BED("Floor -> Room -> Bed"),
    ROOM_BED("Room -> Bed");

    public final String label;

    GroupByEnum(String label) {
        this.label = label;
    }
}
