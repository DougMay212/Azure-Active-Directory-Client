package com.doug.example.oauthclient.microservice.config;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetailsSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.GenericFilterBean;

public class AzureJwtFilter extends GenericFilterBean {

    private static final String USER_INFO_URL = "https://graph.microsoft.com/v1.0/me/";
    private static final String AUTHORIZATION_HEADER_NAME = "Authorization";
    private static final String TOKEN_PREFIX = "bearer ";

    private AzureUserInfoTokenServices tokenServices;
    private RequestMatcher matcher;
    private AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource =
            new OAuth2AuthenticationDetailsSource();

    public AzureJwtFilter(String pattern, String clientId) {
        super();
        matcher = new AntPathRequestMatcher(pattern);
        tokenServices = new AzureUserInfoTokenServices(USER_INFO_URL, clientId);
    }

    @Override
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest)servletRequest;
        HttpServletResponse response = (HttpServletResponse)servletResponse;

        if(!matcher.matches(request)) {
            chain.doFilter(request, response);
            return;
        }

        if (request.getHeader(AUTHORIZATION_HEADER_NAME) == null ||
                !request.getHeader(AUTHORIZATION_HEADER_NAME).toLowerCase().startsWith(TOKEN_PREFIX)) {
            request.setAttribute("ERROR", "Could not obtain Access Token from Authorization Header");
            chain.doFilter(request, response);
            return;
        }
        String base64AccessToken = request.getHeader(AUTHORIZATION_HEADER_NAME).substring(TOKEN_PREFIX.length());
        try {
            OAuth2AccessToken accessToken = new DefaultOAuth2AccessToken(base64AccessToken);
            OAuth2Authentication authentication = this.tokenServices.loadAuthentication(accessToken.getValue());
            request.setAttribute(OAuth2AuthenticationDetails.ACCESS_TOKEN_VALUE, accessToken.getValue());
            request.setAttribute(OAuth2AuthenticationDetails.ACCESS_TOKEN_TYPE, accessToken.getTokenType());
            authentication.setDetails(this.authenticationDetailsSource.buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception e) {
            request.setAttribute("ERROR", "External validation of access token failed");
            request.setAttribute("ERROR_MESSAGE", e.getMessage());
        }
        chain.doFilter(request, response);
    }
}