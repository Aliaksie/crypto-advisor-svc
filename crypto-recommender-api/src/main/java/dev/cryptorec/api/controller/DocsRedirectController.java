package dev.cryptorec.api.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DocsRedirectController {
    @GetMapping("/docs")
    public String redirectToReDoc() {
        return "redirect:/redoc.html";
    }
}
