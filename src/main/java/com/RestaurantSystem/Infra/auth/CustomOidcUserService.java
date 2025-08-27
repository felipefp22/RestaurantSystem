package com.RestaurantSystem.Infra.auth;

import com.RestaurantSystem.Repositories.AuthUserRepository;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

public class CustomOidcUserService extends OidcUserService {

    private final AuthUserRepository authUserRepository;
    private final TokenServiceOur tokenServiceOur;

    public CustomOidcUserService(AuthUserRepository authUserRepository, TokenServiceOur tokenServiceOur) {
        this.authUserRepository = authUserRepository;
        this.tokenServiceOur = tokenServiceOur;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) {
        OidcUser oidcUser = super.loadUser(userRequest);

        return oidcUser;
    }
}
