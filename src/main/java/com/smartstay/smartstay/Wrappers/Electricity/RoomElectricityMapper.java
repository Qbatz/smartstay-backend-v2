package com.smartstay.smartstay.Wrappers.Electricity;

import com.smartstay.smartstay.responses.electricity.RoomElectricityCustomersList;
import com.smartstay.smartstay.util.Utils;

import java.util.function.Function;

public class RoomElectricityMapper implements Function<com.smartstay.smartstay.dto.electricity.RoomElectricityCustomersList, RoomElectricityCustomersList> {
    @Override
    public RoomElectricityCustomersList apply(com.smartstay.smartstay.dto.electricity.RoomElectricityCustomersList roomElectricityCustomersList) {
        StringBuilder initials = new StringBuilder();
        StringBuilder fullName = new StringBuilder();

        if (roomElectricityCustomersList.getFirstName() != null) {
            fullName.append(roomElectricityCustomersList.getFirstName());
            initials.append(roomElectricityCustomersList.getFirstName().toUpperCase().charAt(0));
        }
        if (roomElectricityCustomersList.getLastName() != null) {
            fullName.append(" ");
            fullName.append(roomElectricityCustomersList.getLastName());
            if (!roomElectricityCustomersList.getLastName().trim().equalsIgnoreCase("")) {
                initials.append(roomElectricityCustomersList.getLastName().toUpperCase().charAt(0));
            }
        }
        else {
            initials.append(roomElectricityCustomersList.getLastName().toUpperCase().charAt(1));
        }

        double consumption =  Utils.roundOffWithTwoDigit(roomElectricityCustomersList.getConsumption());
        double amount = Utils.roundOffWithTwoDigit(roomElectricityCustomersList.getAmount());


        return new RoomElectricityCustomersList(roomElectricityCustomersList.getCustomerId(),
                roomElectricityCustomersList.getFirstName(),
                roomElectricityCustomersList.getLastName(),
                fullName.toString(),
                roomElectricityCustomersList.getProfilePic(),
                initials.toString(),
                Utils.dateToString(roomElectricityCustomersList.getStartDate()),
                Utils.dateToString(roomElectricityCustomersList.getStartDate()),
                Utils.dateToString(roomElectricityCustomersList.getEndDate()),
                roomElectricityCustomersList.getBedName(),
                consumption,
                amount);
    }
}
