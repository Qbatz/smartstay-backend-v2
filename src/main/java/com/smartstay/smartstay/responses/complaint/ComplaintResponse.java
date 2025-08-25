package com.smartstay.smartstay.responses.complaint;

import java.util.Date;

public interface ComplaintResponse {
    Integer getComplaintId();
    String getCustomerId();
    Integer getComplaintTypeId();
    String getComplaintTypeName();
    Integer getFloorId();
    Integer getRoomId();
    Date getComplaintDate();
    String getDescription();
    String getStatus();
}
