package com.doug.example.oauthclient.microservice.config;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Component;

@Component
public class AzureJwtProvider implements AuthenticationProvider {

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        OAuth2Authentication oAuth2Authentication = (OAuth2Authentication)authentication;
        //TODO: perform some checks on token fields. In the provider we can autowire services, unlike the filter.
        oAuth2Authentication = new OAuth2Authentication(
                AzureUserInfoTokenServices.cloneRequest(oAuth2Authentication.getOAuth2Request(), true),
                oAuth2Authentication.getUserAuthentication());
        return oAuth2Authentication;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.equals(OAuth2Authentication.class);
    }
}
