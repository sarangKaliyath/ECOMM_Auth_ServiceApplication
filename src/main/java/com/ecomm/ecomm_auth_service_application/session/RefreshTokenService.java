package com.ecomm.ecomm_auth_service_application.session;

import com.ecomm.ecomm_auth_service_application.model.Session;
import com.ecomm.ecomm_auth_service_application.model.State;
import com.ecomm.ecomm_auth_service_application.model.User;
import com.ecomm.ecomm_auth_service_application.repository.SessionRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class RefreshTokenService {

    @Autowired
    SessionRepo sessionRepo;

    public String createRefreshToken(User user) {
        String refreshToken = UUID.randomUUID().toString();

        Session session = new Session();
        session.setUser(user);
        session.setRefreshToken(refreshToken);
        session.setState(State.ACTIVE);
        session.setCreatedAt(new Date());

        session.setExpiresAt(new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7)));

        sessionRepo.save(session);

        return refreshToken;
    }
}
