package com.smartstay.smartstay.responses.banking;

/**
 * One selectable "responsible person" for a hostel — a user mapped to the hostel
 * together with their role. Assembled from three separate lookups (user_hostel,
 * users, rolesv1) so the pieces stay independent for a future microservice split.
 */
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
