package com.smartstay.smartstay.ennum;

public enum ModuleId {
    DASHBOARD(1),
    ANNOUNCEMENT(2),
    UPDATES(3),
    PAYING_GUEST(4),
    CUSTOMERS(5),
    BOOKING(6),
    CHECKOUT(7),
    WALK_IN(8),
    ASSETS(9),
    VENDOR(10),
    BILLS(11),
    RECURRING_BILLS(12),
    COMPLAINTS(13),
    ELECTRIC_CITY(14),
    EXPENSE(15),
    REPORTS(16),
    BANKING(17),
    PROFILE(18),
    AMENITIES(19),
    RECEIPT(20),
    INVOICE(21),
    USER(22),
    ROLES(23),
    AGREEMENT(24);

    private final int id;

    ModuleId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static ModuleId fromId(int id) {
        for (ModuleId module : values()) {
            if (module.id == id) {
                return module;
            }
        }
        throw new IllegalArgumentException("Invalid ModuleId: " + id);
    }
}
