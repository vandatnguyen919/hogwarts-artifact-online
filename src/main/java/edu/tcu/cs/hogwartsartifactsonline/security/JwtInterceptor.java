package edu.tcu.cs.hogwartsartifactsonline.security;

import edu.tcu.cs.hogwartsartifactsonline.client.rediscache.RedisCacheClient;
import io.lettuce.core.RedisChannelHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/* Spring Interceptor
* An interceptor intercepts and processes incoming HTTP requests and outgoing responses
* at a global level before they reach the controller or after they leave the controller.
*
* NOTE: Spring Security is invoked before interceptors.
* */

@Component
public class JwtInterceptor implements HandlerInterceptor {

    private final RedisCacheClient redisCacheClient;

    public JwtInterceptor(RedisCacheClient redisCacheClient) {
        this.redisCacheClient = redisCacheClient;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Get the token from the request header
        String authorizationHeader = request.getHeader("Authorization");

        // If the token is not null, and it starts with "Bearer ", then we need to verify if this token is present in Redis
        // Else this request is just a public request that does not need a token. E.g., login, register, etc.
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Jwt jwt = (Jwt) authentication.getPrincipal();

            // Retrieve the userId from the Jwt claims and check if the token is in the Redis whitelist or not
            String userId = jwt.getClaimAsString("userId");
            if (!this.redisCacheClient.isUserTokenInWhiteList(userId, jwt.getTokenValue())) {
                throw new BadCredentialsException("Invalid token");
            }
        }
        return true;
    }
}
