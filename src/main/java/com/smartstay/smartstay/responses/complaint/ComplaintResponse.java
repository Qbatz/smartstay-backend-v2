package com.smartstay.smartstay.responses.complaint;

import java.util.Date;

public interface ComplaintResponse {
    Integer getComplaintId();
    String getCustomerId();
    String getCustomerName();
    String getCustomerProfile();
    String getHostelId();

    Integer getFloorId();
    String getFloorName();

    Integer getRoomId();
    String getRoomName();

    Integer getBedId();
    String getBedName();

    Date getComplaintDate();
    String getDescription();
    String getAssigneeName();

    Integer getComplaintTypeId();
    String getComplaintTypeName();
    String getStatus();
}
