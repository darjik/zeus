package org.zeus.ims.config;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    @ModelAttribute("request")
    public HttpServletRequest request(HttpServletRequest request) {
        return request;
    }

    @ModelAttribute("session")
    public HttpSession session(HttpServletRequest request) {
        return request.getSession(false);
    }

    @ModelAttribute("servletContext")
    public ServletContext servletContext(HttpServletRequest request) {
        return request.getServletContext();
    }

    @ModelAttribute("response")
    public HttpServletResponse response(HttpServletResponse response) {
        return response;
    }
}
