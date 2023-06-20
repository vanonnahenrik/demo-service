package de.dsa.prodis.service.registry.rest.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/v1")
public class RegistryController {
    private final static Logger LOG = LoggerFactory.getLogger(RegistryController.class);

    @GetMapping("/hello")
    public String hello() {
        return "HELLO WORLD";
    }
}
