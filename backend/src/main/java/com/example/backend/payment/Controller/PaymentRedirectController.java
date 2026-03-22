package com.example.backend.payment.Controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PaymentRedirectController {

    @GetMapping("/payment/success")
    public String success() {
        return "forward:/payment/success.html";
    }
}
