package com.usta.serviexpress.payments.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class WompiResultController {

    @GetMapping("/pagos/wompi/callback")
    public String callback(@RequestParam(name = "id", required = false) String wompiTxId,
                           Model model) {
        model.addAttribute("wompiTxId", wompiTxId);
        return "wompi_result"; // templates/wompi_result.html
    }
}