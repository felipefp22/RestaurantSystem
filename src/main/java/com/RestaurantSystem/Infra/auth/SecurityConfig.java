package com.RestaurantSystem.Infra.auth;

import com.RestaurantSystem.Entities.User.AuthUserLogin;
import com.RestaurantSystem.Entities.User.RefreshToken;
import com.RestaurantSystem.Repositories.AuthUserRepository;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Value("${oauth.jwt.secret}")
    private String secret;
    private final SecurityFilter securityFilter;
    private final AuthUserRepository authUserRepository;
    private final TokenServiceOur tokenServiceOur;
    private final ClientRegistrationRepository clientRegistrationRepository;


    @Autowired
    public SecurityConfig(SecurityFilter securityFilter, AuthUserRepository authUserRepository, TokenServiceOur tokenServiceOur, ClientRegistrationRepository clientRegistrationRepository) {
        this.securityFilter = securityFilter;
        this.authUserRepository = authUserRepository;
        this.tokenServiceOur = tokenServiceOur;
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOriginPatterns(List.of("*")); // Use this instead of setAllowedOrigins
                    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS", "TRACE", "CONNECT"));
                    config.setAllowedHeaders(List.of("*"));
                    config.setAllowCredentials(true);
                    config.setMaxAge(3600L);
                    return config;
                }))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeRequests(auth -> {
                    auth.requestMatchers("/auth/register").permitAll();
                    auth.requestMatchers("/auth/login").permitAll();
                    auth.requestMatchers("/auth/refresh-token").permitAll();
                    auth.requestMatchers("/auth/request-confirm-account").permitAll();
                    auth.requestMatchers("/auth/request-reset-password").permitAll();
                    auth.requestMatchers("/auth/get-token-reset-password/{code}").permitAll();
                    auth.requestMatchers("/auth/reset-password").permitAll();
                    auth.requestMatchers("/auth/confirm-account").permitAll();
                    auth.requestMatchers("/auth/confirm-account-via-code/{code}").permitAll();
                    auth.requestMatchers("/auth/oauth2-hiring/callbacks/oauth2").permitAll();
                    auth.requestMatchers("/auth/request-delete-account-token").permitAll();
                    auth.requestMatchers("/auth/delete-account-via-code/{deleteCode}").permitAll();

                    auth.requestMatchers("/advertise/get-advertises-to-map").permitAll();
                    auth.requestMatchers("/advertise/get-advertise-by-categoryID").permitAll();
                    auth.requestMatchers("/advertise/get-advertises-categories-available").permitAll();

                    auth.requestMatchers("/helper-dialogs/helpers-texts").permitAll();

                    auth.requestMatchers("/webhook-receives/mp-payments").permitAll();

                    // Internal use, just to communicate with HiringMP (Our payment API)
                    //auth.requestMatchers("/credits-internal-use/get-credits-by-payments-ids").hasRole("HIRINGPAYMENTSADMIN");
                    //auth.requestMatchers("/credits-internal-use/create-credits-paid").hasRole("HIRINGPAYMENTSADMIN");

                    auth.requestMatchers("/adm/**").hasRole("ADMIN");
                    auth.requestMatchers("/adm-master/**").hasRole("MASTERADMIN");
                    auth.anyRequest().hasRole("USER");
                })
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(authorization ->
                                authorization.authorizationRequestResolver(
                                        new CustomAuthorizationRequestResolver(clientRegistrationRepository)
                                )
                        )
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(customOidcUserService())
                                .userService(customOAuth2UserService()))
                        .successHandler((request, response, authentication) -> {
                            String state = request.getParameter("state");
                            String platform = "unknown";
                            if (state != null) {
                                try {
                                    platform = new String(Base64.getDecoder().decode(state), StandardCharsets.UTF_8);
                                } catch (IllegalArgumentException ignored) {}
                            }

                            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

                            String refreshToken = findEmailOrCreateUserReturnRefreshToken(oAuth2User.getAttribute("email"), oAuth2User.getAttribute("name"));

                            HttpSession session = request.getSession();
                            session.setAttribute("refreshToken", refreshToken);
                            session.setAttribute("platform", platform);

                            response.sendRedirect("/auth/oauth2-hiring/callbacks/oauth2");
                        })
                )
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden: You don't have the necessary permissions");
                        })
                        .authenticationEntryPoint((request, response, authException) -> {
                            // Do nothing; let Spring Boot handle the exception
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User is not authenticated");
                        })

                )
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

//    @Bean
//    CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//        configuration.setAllowedOrigins(List.of("*")); // Allow all origins
//        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
//        configuration.setAllowedHeaders(List.of("*"));
//        configuration.setExposedHeaders(List.of("Authorization", "Content-Type")); // Expose necessary headers
//        configuration.setAllowCredentials(true);
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", configuration);
//        return source;
//    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {

        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

    @Bean
    public CustomOAuth2UserService customOAuth2UserService() {
        return new CustomOAuth2UserService(authUserRepository, tokenServiceOur);
    }

    @Bean
    public CustomOidcUserService customOidcUserService() {
        return new CustomOidcUserService(authUserRepository, tokenServiceOur);
    }

    // <>--------------- Methods ---------------<>
    private String findEmailOrCreateUserReturnRefreshToken(String email, String name) {
        AuthUserLogin user = authUserRepository.findById(email)
                .orElseGet(() -> new AuthUserLogin(email, name, null, UUID.randomUUID().toString(), true));

        authUserRepository.save(user);

        // Generate refreshToken for login after OAuth2
        RefreshToken refreshToken = tokenServiceOur.createRefreshToken(user, "SocialLogin");

        return refreshToken.getId().toString();
    }
}

//http://localhost:4030/oauth2/authorization/github
