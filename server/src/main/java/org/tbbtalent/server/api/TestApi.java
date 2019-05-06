package org.tbbtalent.server.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/test")
public class TestApi {

    @GetMapping
    public String getTest() {
        return "Success, it is working!";
    }
}
