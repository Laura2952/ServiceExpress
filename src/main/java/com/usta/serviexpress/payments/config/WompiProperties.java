package com.usta.serviexpress.payments.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * WompiProperties
 *
 * Purpose:
 * - Maps configuration properties related to the Wompi payment gateway.
 * - Uses Spring Boot's @ConfigurationProperties to bind external properties (e.g., application.yml or application.properties).
 *
 * Properties:
 * - publicKey: The public API key provided by Wompi for frontend/payment requests.
 * - integritySecret: Secret key for verifying webhook signatures (HMAC-SHA256).
 * - currency: Default currency for payments (e.g., "COP").
 * - redirectUrl: URL to redirect users after completing a payment.
 * - useWidget: Boolean flag to indicate whether to use Wompi's frontend widget (default: true).
 *
 * Usage Notes:
 * - IntegritySecret must be kept confidential and never exposed on the frontend.
 * - These properties are typically configured in application.yml like:
 *
 *   wompi:
 *     public-key: <your-public-key>
 *     integrity-secret: <your-secret>
 *     currency: COP
 *     redirect-url: https://yourdomain.com/checkout/complete
 *     use-widget: true
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "wompi")
public class WompiProperties {

    /**
     * Public API key used to initialize Wompi frontend or SDK.
     */
    private String publicKey;

    /**
     * Secret key to verify webhook integrity (HMAC-SHA256).
     */
    private String integritySecret;

    /**
     * Default currency code for transactions.
     */
    private String currency;

    /**
     * Redirect URL after successful or failed payment.
     */
    private String redirectUrl;

    /**
     * Whether to use Wompi's payment widget (default true).
     */
    private boolean useWidget = true;
}

/*
Summary (Technical Note):
WompiProperties is a Spring Boot configuration class that binds external properties related
to the Wompi payment gateway. It centralizes configuration for API keys, webhooks, currency,
redirection behavior, and widget usage. It allows the application to easily switch environments
or update keys without code changes.
*/
