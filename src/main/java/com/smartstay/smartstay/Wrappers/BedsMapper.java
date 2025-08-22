package com.smartstay.smartstay.Wrappers;

import com.smartstay.smartstay.dao.Beds;
import com.smartstay.smartstay.responses.beds.BedsResponse;

import java.util.function.Function;

public class BedsMapper implements Function<Beds, BedsResponse> {
    @Override
    public BedsResponse apply(Beds beds) {
        return new BedsResponse(beds.getBedId(),
                beds.getBedName(),
                beds.getRoomId(),
                false,
                false,
                false,
                null,
                beds.getRentAmount());
    }
}
