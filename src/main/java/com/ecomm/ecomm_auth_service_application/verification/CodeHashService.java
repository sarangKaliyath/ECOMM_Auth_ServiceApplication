package com.ecomm.ecomm_auth_service_application.verification;

import com.ecomm.ecomm_auth_service_application.security.TokenHasher;
import org.springframework.stereotype.Service;

@Service
public class CodeHashService {

    public String hash(String rawCode) {
        return TokenHasher.hash(rawCode);
    }

    public boolean matches(String rawCode, String codeHash) {
        return TokenHasher.hash(rawCode).equals(codeHash);
    }
}
