package com.usta.serviexpress.payments.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * WompiResultController
 *
 * Purpose:
 * - Handles the callback from Wompi after a payment attempt.
 * - Receives the transaction ID sent by Wompi and passes it to a view for display.
 *
 * Notes:
 * - This endpoint is typically used after redirection from Wompi's payment page.
 * - Minimal logic is performed; primarily forwards the transaction ID to the frontend template.
 *
 * Limitations / Considerations:
 * - No signature or integrity check is performed here; consider verifying the transaction via
 *   Wompi's API or webhook for production-grade reliability.
 * - The `id` parameter is optional; null values are handled gracefully in the view.
 */
@Controller
public class WompiResultController {

    /**
     * Handles Wompi callback and renders a result page.
     *
     * @param wompiTxId Optional transaction ID provided by Wompi ("id" query parameter).
     * @param model Spring MVC Model to pass attributes to the view.
     * @return Thymeleaf template name "wompi_result" to display transaction outcome.
     *
     * Process:
     * 1. Receives the "id" query parameter from Wompi redirect (transaction ID).
     * 2. Adds the transaction ID to the model for rendering in the view.
     * 3. Returns the "wompi_result" template.
     */
    @GetMapping("/pagos/wompi/callback")
    public String callback(@RequestParam(name = "id", required = false) String wompiTxId,
                           Model model) {
        model.addAttribute("wompiTxId", wompiTxId);
        return "wompi_result"; // templates/wompi_result.html
    }
}

/*
Summary (Technical Note):
WompiResultController handles the redirection after a Wompi payment. It receives an optional
transaction ID from the query parameter, stores it in the model, and forwards the request to
the "wompi_result" Thymeleaf template. No payment validation or webhook handling is performed here;
it is purely for displaying the result of the payment attempt to the user.
*/
