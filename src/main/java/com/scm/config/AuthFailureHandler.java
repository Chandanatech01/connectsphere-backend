package com.scm.config;

import java.io.IOException;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.scm.helpers.Message;
import com.scm.helpers.MessageType;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class AuthFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {

        HttpSession session = request.getSession();

        if (exception instanceof DisabledException) {
            session.setAttribute("message",
                    Message.builder()
                            .content("Your account is disabled. A verification link has been sent to your email.")
                            .type(MessageType.red)
                            .build());
        } else if (exception instanceof BadCredentialsException) {
            session.setAttribute("message",
                    Message.builder()
                            .content("Invalid email or password. Please try again.")
                            .type(MessageType.red)
                            .build());
        } else {
            session.setAttribute("message",
                    Message.builder()
                            .content("Authentication failed. Please try again.")
                            .type(MessageType.red)
                            .build());
        }

        response.sendRedirect("/login");
    }
}