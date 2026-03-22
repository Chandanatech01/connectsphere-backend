package com.scm.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpSession;

@Component
public class SessionHelper {

    private static final Logger logger = LoggerFactory.getLogger(SessionHelper.class);

    public static void removeMessage() {
        try {
            ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes == null) {
                logger.warn("No request attributes found, cannot remove message from session.");
                return;
            }

            HttpSession session = attributes.getRequest().getSession(false);
            if (session != null) {
                session.removeAttribute("message");
                logger.debug("Message removed from session successfully.");
            }

        } catch (Exception e) {
            logger.error("Error removing message from session: {}", e.getMessage());
        }
    }
}