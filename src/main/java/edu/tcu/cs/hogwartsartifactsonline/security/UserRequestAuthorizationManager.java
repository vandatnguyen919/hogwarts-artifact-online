package edu.tcu.cs.hogwartsartifactsonline.security;

import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriTemplate;

import java.util.Map;
import java.util.function.Supplier;

@Component
public class UserRequestAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private static final UriTemplate USER_URI_TEMPLATE = new UriTemplate("/users/{userId}");

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authenticationSupplier, RequestAuthorizationContext context) {
        // Extract the userId from the request URI: /users/{userId}
        String url = context.getRequest().getRequestURI();
        Map<String, String> uriVariables = USER_URI_TEMPLATE.match(context.getRequest().getRequestURI());
        String userIdFromRequestUri = uriVariables.get("userId");

        // Extract the userId from the Authentication object, which is a Jwt object
        Authentication authentication = authenticationSupplier.get();
        String userIdFromJwt = ((Jwt) authentication.getPrincipal()).getClaimAsString("userId");

        // Check if the user has the role "ROLE_user"
        boolean hasUserRole = authentication.getAuthorities().stream().anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_user"));

        // Check if the user has the role "ROLE_admin"
        boolean hasAdminRole = authentication.getAuthorities().stream().anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_admin"));

        // Compare the two userIds.
        boolean userIdsMatch = userIdFromRequestUri != null && userIdFromRequestUri.equals(userIdFromJwt);
        return new AuthorizationDecision(hasAdminRole || (hasUserRole && userIdsMatch));
    }
}
