package uk.ac.ebi.pride.ws.pride.security.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class SimpleCORSFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (((request.getHeader("Access-Control-Request-Method") != null
                || ("GET".equals(request.getMethod()))
                || ("POST".equals(request.getMethod())))
                || ("OPTIONS".equals(request.getMethod()))
                || ("DELETE".equals(request.getMethod()))
                || ("PUT".equals(request.getMethod())))) {
            response.addHeader("Access-Control-Allow-Origin", "*");
            response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.addHeader("Access-Control-Allow-Headers", "Content-Type,X-Requested-With,accept,Origin,Access-Control-Request-Method,Access-Control-Request-Headers,Access-Control-Allow-Headers, Authorization");

        }
        log.info("########### Simple CORS Filter: " + request.getRequestURL() + " ############");

        filterChain.doFilter(request, response);
    }
}