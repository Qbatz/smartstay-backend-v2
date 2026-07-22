package com.smartstay.smartstay.responses.banking;

public record ResponsiblePersonResponse(
        String userId,
        String firstName,
        String lastName,
        int roleId,
        String role,
        String hostelId,
        String parentId
) {
}
