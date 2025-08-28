package com.RestaurantSystem.Entities.User.AdmDTOs;

import com.RestaurantSystem.Entities.User.AuthUserLogin;

public record UserSocialAbbreviatedAdminDTO(
// User Social Abbreviated is used for when person searching another user, in live search bar (like facebook user search)
// If Patient appears like[ photo - username (email) like facebook user search
// If Doctor appears like[ photo - username (email) - (DOCTOR) ] like facebook user search

        String photoURL,
        String email,
        String name,
        String phone,
        String username,
        Boolean isAdmin

) {
    public UserSocialAbbreviatedAdminDTO(AuthUserLogin authUserLogin, String requestID, Boolean isAdmin) {
        this(authUserLogin.getUrlProfilePhoto(), authUserLogin.getEmail(), authUserLogin.getName(), authUserLogin.getPhone(), authUserLogin.getUsername(), isAdmin);

    }

}
