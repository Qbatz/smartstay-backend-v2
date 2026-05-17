package com.smartstay.smartstay.filterOptions.bookings;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingsFilterOptions {

    List<FilterItems> floors;
    List<RoomsItem> rooms;
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FilterItems {
        private String name;
        private String type;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RoomsItem {
        private String roomName;
        private String roomId;
        private String floorId;
    }
}
