package com.kmair.KYUpdateHelper;

import java.util.Date;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class WelcomeController {
    private static final Logger logger = Logger.getLogger(WelcomeController.class);

    @Value("${application.message:Hello World}")
    private String message = "Hello World";

    @RequestMapping("/welcome")
    public String welcome(Model model) {
        model.addAttribute("time", new Date());
        model.addAttribute("message", this.message);
        return "welcome";
    }

    @RequestMapping("/foo")
    public String foo(Model model) {
        throw new RuntimeException("Foo");
    }

}
