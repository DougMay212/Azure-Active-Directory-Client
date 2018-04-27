package com.doug.example.oauthclient.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SimpleRestController {

    final static Logger LOG = LoggerFactory.getLogger(SimpleRestController.class);

    @GetMapping("/greeting")
    public String greet(@RequestParam(required = false, defaultValue = "World") String name) {
        LOG.info("Requesting a message for name {}...", name);
        return "{\"value\": \"Hello " + name + "!\"}";
    }
}
