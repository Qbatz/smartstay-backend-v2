package com.smartstay.smartstay.responses.complaint;

import java.util.List;

public record ComplaintsUpdates(Integer complaintId, List<ComplaintUpdatesList> complaintUpdates) {
}
