package com.doug.example.oauthclient.microservice.config;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetailsSource;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

public class AzureJwtFilter extends AbstractAuthenticationProcessingFilter {

    private static final String USER_INFO_URL = "https://graph.microsoft.com/v1.0/me/";
    private static final String AUTHORIZATION_HEADER_NAME = "Authorization";
    private static final String TOKEN_PREFIX = "bearer ";

    private UserInfoTokenServices tokenServices;
    private AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource =
            new OAuth2AuthenticationDetailsSource();

    public AzureJwtFilter(String pattern, String clientId) {
        super(new AntPathRequestMatcher(pattern));
        tokenServices = new UserInfoTokenServices(USER_INFO_URL, clientId);
        this.setAuthenticationDetailsSource(authenticationDetailsSource);
    }

    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException {
        if (request.getHeader(AUTHORIZATION_HEADER_NAME) == null ||
                !request.getHeader(AUTHORIZATION_HEADER_NAME).toLowerCase().startsWith(TOKEN_PREFIX)) {
            throw new BadCredentialsException("Could not obtain Access Token from Authorization Header");
        }
        String base64AccessToken = request.getHeader(AUTHORIZATION_HEADER_NAME).substring(TOKEN_PREFIX.length());
        OAuth2AccessToken accessToken = new DefaultOAuth2AccessToken(base64AccessToken);
        OAuth2Authentication authentication = this.tokenServices.loadAuthentication(accessToken.getValue());
        if (this.authenticationDetailsSource != null) {
            request.setAttribute(OAuth2AuthenticationDetails.ACCESS_TOKEN_VALUE, accessToken.getValue());
            request.setAttribute(OAuth2AuthenticationDetails.ACCESS_TOKEN_TYPE, accessToken.getTokenType());
            authentication.setDetails(this.authenticationDetailsSource.buildDetails(request));
        }
        return authentication;
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult)
            throws IOException, ServletException {
        SecurityContextHolder.getContext().setAuthentication(authResult);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        SecurityContextHolder.clearContext();
        throw new BadCredentialsException("Token rejected when retrieving user info");
    }
}