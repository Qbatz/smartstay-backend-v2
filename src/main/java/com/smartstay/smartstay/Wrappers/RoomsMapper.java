package com.smartstay.smartstay.Wrappers;

import com.smartstay.smartstay.dao.Rooms;
import com.smartstay.smartstay.responses.rooms.RoomsResponse;

import java.util.function.Function;

public class RoomsMapper implements Function<Rooms, RoomsResponse> {
    @Override
    public RoomsResponse apply(Rooms rooms) {
        return new RoomsResponse(rooms.getRoomId(), rooms.getRoomName(),rooms.getFloorId());
    }
}
