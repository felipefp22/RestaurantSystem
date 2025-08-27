package com.RestaurantSystem.Entities.User.SocialUserDTOs;


import com.RestaurantSystem.Entities.User.AuthUserLogin;

public record UserSocialAbbreviatedDTO(
// User Social Abbreviated is used for when person searching another user, in live search bar (like facebook user search)
// If Patient appears like[ photo - username (email) like facebook user search
// If Doctor appears like[ photo - username (email) - (DOCTOR) ] like facebook user search

        String photoURL,
        String email,
        String name,
        String phone,
        String username
) {
    public UserSocialAbbreviatedDTO(AuthUserLogin authUserLogin, String requestID) {
        this(authUserLogin.getUrlProfilePhoto(), authUserLogin.getEmail(), authUserLogin.getName(), authUserLogin.getPhone(), authUserLogin.getUsername());

    }

}
