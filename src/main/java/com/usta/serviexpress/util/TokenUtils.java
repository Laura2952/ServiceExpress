package com.usta.serviexpress.util;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TokenUtils {
    public String newToken() {
        return UUID.randomUUID().toString().replace("-", ""); // genera un token de 32 chars
    }
}

