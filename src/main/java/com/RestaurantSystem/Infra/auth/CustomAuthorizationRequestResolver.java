package com.RestaurantSystem.Infra.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class CustomAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {
    private final OAuth2AuthorizationRequestResolver defaultResolver;

    public CustomAuthorizationRequestResolver(ClientRegistrationRepository repo) {
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(repo, "/oauth2/authorization");
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        return customize(defaultResolver.resolve(request), request);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        return customize(defaultResolver.resolve(request, clientRegistrationId), request);
    }

    private OAuth2AuthorizationRequest customize(OAuth2AuthorizationRequest request, HttpServletRequest httpRequest) {
        if (request == null) return null;
        System.out.println(httpRequest.getHeader("Referer"));
        String platform =
                httpRequest.getHeader("Referer").contains("https://felipefp22.github.io/FelipeFPortfolio") ? "prod" :
                        httpRequest.getHeader("Referer").contains("xpto.com.br") ? "dev" :
                                httpRequest.getHeader("Referer").contains("localhost:5173") ? "localhost" : null;

        Map<String, Object> additionalParams = new HashMap<>(request.getAdditionalParameters());
        if (platform != null) {
            additionalParams.put("platform", platform);
        }

        return OAuth2AuthorizationRequest.from(request)
                .state(Base64.getEncoder().encodeToString(platform.getBytes(StandardCharsets.UTF_8))) // encoded in state
                .additionalParameters(additionalParams)
                .build();
    }
}