package com.doug.example.oauthclient.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class LandingPageController {

    final static Logger LOG = LoggerFactory.getLogger(LandingPageController.class);

    @RequestMapping(value = "/")
    public String index() {
        LOG.info("--- Base URL requested ---");
        return "index";
    }

    @RequestMapping(value = "/loggedout")
    public String loggedout() {
        LOG.info("--- Log out page requested ---");
        return "loggedout";
    }
}
