package com.smartstay.smartstay.Wrappers;

import com.smartstay.smartstay.dao.Floors;
import com.smartstay.smartstay.responses.floors.FloorsResponse;

import java.util.function.Function;

public class FloorsMapper implements Function<Floors, com.smartstay.smartstay.responses.floors.FloorsResponse> {
    @Override
    public FloorsResponse apply(Floors floors) {
        return new FloorsResponse(floors.getFloorId(), floors.getFloorName());
    }
}
