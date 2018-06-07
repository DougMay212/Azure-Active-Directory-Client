package com.doug.example.oauthclient.microservice.config;

import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;

public class AzureUserInfoTokenServices extends UserInfoTokenServices {

    public AzureUserInfoTokenServices(String userInfoEndpointUrl, String clientId) {
        super(userInfoEndpointUrl, clientId);
    }

    public OAuth2Authentication loadAuthentication(String accessToken) throws AuthenticationException, InvalidTokenException {
        OAuth2Authentication authentication = super.loadAuthentication(accessToken);
        // must set approved to false in order to reach the AzureJwtProvider
        OAuth2Request request = cloneRequest(authentication.getOAuth2Request(), false);
        authentication = new OAuth2Authentication(request, authentication.getUserAuthentication());
        return authentication;
    }

    public static OAuth2Request cloneRequest(OAuth2Request original, boolean approved) {
        return new OAuth2Request(original.getRequestParameters(), original.getClientId(),
                original.getAuthorities(), approved, original.getScope(),
                original.getResourceIds(), original.getRedirectUri(),
                original.getResponseTypes(), original.getExtensions());
    }
}
