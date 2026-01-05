package com.smartstay.smartstay.responses.complaint;

import java.util.List;

public record ComplaintUpdates(String update,
                               String description,
                               String updatedBy,
                               String initials,
                               String profilePic,
                               String updatedAt,
                               String updatedTime,
                               List<ComplaintComments> comments) {
}
