package com.usta.serviexpress.payments.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter @Setter
@Configuration
@ConfigurationProperties(prefix = "wompi")
public class WompiProperties {
    private String publicKey;
    private String integritySecret;
    private String currency;
    private String redirectUrl;
    private boolean useWidget = true;
}