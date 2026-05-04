package br.com.escola.accesscontrol.adapter.in.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthTestController {

    @GetMapping("/api/test/authenticated")
    public String authenticated() {
        return "authenticated";
    }
}
