package com.example.JavaTask.controllers;

import com.example.JavaTask.models.Payload;
import com.example.JavaTask.services.PayloadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PayloadController {

    @Autowired
    private PayloadService payloadService;

    private static Logger logger = LoggerFactory.getLogger(PayloadController.class);

    @GetMapping("/healthz")
    public String isLive(){
        logger.info("GET request received at /healthz");
        logger.info("The application is running perfectly..!!");
        return "OK";
    }

    @PostMapping("/log")
    public Payload logPayload(@RequestBody Payload payload){
        logger.info("POST request received at /log");
        return payloadService.logService(payload);
    }
}