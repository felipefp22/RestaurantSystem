package com.RestaurantSystem.Infra.auth;

import com.RestaurantSystem.Repositories.AuthUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final AuthUserRepository authUserRepository;
    private final TokenServiceOur tokenServiceOur;

    @Autowired
    public CustomOAuth2UserService(AuthUserRepository authUserRepository, TokenServiceOur tokenServiceOur) {
        this.authUserRepository = authUserRepository;
        this.tokenServiceOur = tokenServiceOur;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        return oAuth2User;
    }
}