package com.doug.example.oauthclient.ui.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.GenericFilterBean;

public class ExpiredAccessTokenFilter extends GenericFilterBean {

    private static final int ACCESS_TOKEN_BODY_INDEX = 1;
    private static final String ACCESS_TOKEN_EXPIRATION_DATE_KEY = "exp";

    private RequestMatcher requestExclusionMatcher;

    public ExpiredAccessTokenFilter(String exclude) {
        super();
        requestExclusionMatcher = new AntPathRequestMatcher(exclude);
    }

    @Override
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest)servletRequest;
        HttpServletResponse response = (HttpServletResponse)servletResponse;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(requestExclusionMatcher.matches(request)) {
            chain.doFilter(request, response);
            return;
        }

        if(!(authentication instanceof OAuth2Authentication)) {
            chain.doFilter(request, response);
            return;
        }

        Date expirationDate = extractExpirationDate(
                ((OAuth2AuthenticationDetails)authentication.getDetails()).getTokenValue());
        if(expirationDate == null) {
            chain.doFilter(request, response);
            return;
        }

        Date now = new Date();
        if(now.after(expirationDate)) {
            SecurityContextHolder.clearContext();
        }

        chain.doFilter(request, response);
    }

    public static Date extractExpirationDate(String accessToken) {
        String[] accessTokenParts = accessToken.split("\\.");
        if(accessTokenParts.length != 3) return null;

        try {
            Map accessTokenBody = new ObjectMapper().readValue(
                    Base64.getUrlDecoder().decode(accessTokenParts[ACCESS_TOKEN_BODY_INDEX]), Map.class);
            if(accessTokenBody.containsKey(ACCESS_TOKEN_EXPIRATION_DATE_KEY) &&
                    accessTokenBody.get(ACCESS_TOKEN_EXPIRATION_DATE_KEY) instanceof Integer) {
                return new Date(((Integer) accessTokenBody.get(ACCESS_TOKEN_EXPIRATION_DATE_KEY)).longValue() * 1000L);
            }
        } catch (IOException exception) {
            return null;
        }

        return null;
    }
}
