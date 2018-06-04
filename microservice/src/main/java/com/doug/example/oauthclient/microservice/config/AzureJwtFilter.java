package com.doug.example.oauthclient.microservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.Map;
import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.VerificationJwkSelector;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.lang.JoseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.GenericFilterBean;

public class AzureJwtFilter extends GenericFilterBean {
    private final static Logger LOG = LoggerFactory.getLogger(AzureJwtFilter.class);

    private final static String AUTHORIZATION_HEADER_NAME = "Authorization";
    private final static String TOKEN_PREFIX = "token ";
    private final static int METADATA_INDEX = 0;
    private final static int BODY_INDEX = 1;
    private final static int SIGNATURE_INDEX = 2;

    private String azurePublicKeyUrl = "https://login.microsoftonline.com/lowes.onmicrosoft.com/discovery/v2.0/keys";

    private RestOperations restTemplate = new RestTemplate();
    private ObjectMapper objectMapper = new ObjectMapper();
    private VerificationJwkSelector publicKeySelector = new VerificationJwkSelector();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) {

        HttpServletRequest httpRequest = (HttpServletRequest)request;
        if(httpRequest.getHeader(AUTHORIZATION_HEADER_NAME) == null ||
                !httpRequest.getHeader(AUTHORIZATION_HEADER_NAME).startsWith(TOKEN_PREFIX) ) {
            throw new AuthenticationCredentialsNotFoundException("Missing or Invalid Authorization Header in Request");
        }

        String authorizationToken = httpRequest.getHeader(AUTHORIZATION_HEADER_NAME).replace(TOKEN_PREFIX, "");
        String[] tokenParts = authorizationToken.split("\\.");
        if(tokenParts.length != 3) {
            throw new InvalidTokenException("The authorization token did not have 3 parts");
        }
        try {
            byte[] base64DecodedMetadata = Base64.getDecoder().decode(tokenParts[METADATA_INDEX]);
            byte[] base64DecodedBody = Base64.getDecoder().decode(tokenParts[BODY_INDEX]);
            Map<String, String> jwtMetadata = objectMapper.readValue(base64DecodedMetadata, Map.class);
            Map<String, String> jwtBody = objectMapper.readValue(base64DecodedBody, Map.class);
            LOG.info("JWT Metadata (size {}): {}", jwtMetadata.size(), new String(base64DecodedMetadata));
            LOG.info("JWT Body (size {}): {}", jwtBody.size(), new String(base64DecodedBody));
        } catch (IOException e) {
            LOG.error("Malformed Azure Json Web Token");
        }

        try {
            JsonWebSignature signature = new JsonWebSignature();

            String publicKeySetJson = restTemplate.getForObject(
                    new URI(azurePublicKeyUrl), String.class);
            JsonWebKeySet publicKeySet = new JsonWebKeySet(publicKeySetJson);

            signature.setAlgorithmConstraints(new AlgorithmConstraints(
                    AlgorithmConstraints.ConstraintType.WHITELIST,
                    AlgorithmIdentifiers.RSA_USING_SHA256));
            signature.setCompactSerialization(authorizationToken);
            JsonWebKey jsonWebKey = publicKeySelector.select(
                    signature, publicKeySet.getJsonWebKeys());
            signature.setKey(jsonWebKey.getKey());

            if(!signature.verifySignature()) {
                throw new RuntimeException("JSON Web Token Signature Invalid");
            }
        } catch (URISyntaxException e) {
            LOG.error("Invalid URL \"" + azurePublicKeyUrl + "\"", e);
        } catch (JoseException e) {
            LOG.error("An error occurred when validating the JWT signature", e);
            throw new RuntimeException(e);
        }

        try {
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
