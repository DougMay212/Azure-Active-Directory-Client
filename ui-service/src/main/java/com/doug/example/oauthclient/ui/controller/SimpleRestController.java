package com.doug.example.oauthclient.ui.controller;

import java.net.URI;
import java.net.URISyntaxException;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class SimpleRestController {

    final static Logger LOG = LoggerFactory.getLogger(SimpleRestController.class);

    private RestTemplate restTemplate = new RestTemplate();

    @Value("${doug.example.microservice.protocol:http}")
    private String protocol;

    @Value("${doug.example.microservice.host}")
    private String host;

    @Value("${doug.example.microservice.port:}")
    private Integer port;

    @Value("${doug.example.microservice.baseUri}")
    private String baseUri;

    @GetMapping("/greeting")
    public ResponseEntity<String> greet(
            @RequestParam(required = false, defaultValue = "World") String name,
            HttpServletRequest request,
            HttpMethod method) {
        URI microserviceUri = createUri(protocol, host, port,
                baseUri + request.getRequestURI(),
                request.getQueryString());

        LOG.info("Sending request to microservice at {}", microserviceUri.toString());

        return restTemplate.exchange(microserviceUri, method,
                new HttpEntity<>("", new HttpHeaders()), String.class);
    }

    private static URI createUri(String protocol,
                                 String host,
                                 Integer port,
                                 String path,
                                 String queryString) {
        try {
            return port != null ?
                    new URI(protocol, null, host, port, path, queryString, null) :
                    new URI(protocol, host, path, queryString, null);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
