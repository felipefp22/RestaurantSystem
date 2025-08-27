package com.RestaurantSystem.Infra.auth;

import com.RestaurantSystem.Entities.User.AuthUserLogin;
import com.RestaurantSystem.Repositories.AuthUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityFilter extends OncePerRequestFilter {
    private final RetriveAuthInfosService retriveAuthInfosService;
    private final AuthUserRepository authUserRepository;

    public SecurityFilter(RetriveAuthInfosService retriveAuthInfosService, AuthUserRepository authUserRepository) {
        this.retriveAuthInfosService = retriveAuthInfosService;
        this.authUserRepository = authUserRepository;
    }


    // <>--------------- Methodos ---------------<>

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        var token = this.recoverToken(request);
        if(token != null){
            var login = retriveAuthInfosService.retrieveEmailOfUser(token);
            AuthUserLogin user =
                    authUserRepository.findById(login).orElseThrow(()-> new RuntimeException("Invalid Token"));

            var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }

    private String recoverToken(HttpServletRequest request){
        var authHeader = request.getHeader("Authorization");
        if(authHeader == null) return null;
        return authHeader.replace("Bearer ", "");
    }
}
