package com.ai.sakha.logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component

public class LoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);

    @SuppressWarnings("null")
    @Override

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);

        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        Instant startTime = Instant.now();

        try {

            filterChain.doFilter(wrappedRequest, wrappedResponse);

        } finally {

            long duration = Instant.now().toEpochMilli() - startTime.toEpochMilli();

            String reqBody = new String(wrappedRequest.getContentAsByteArray(), StandardCharsets.UTF_8);

            String resBody = new String(wrappedResponse.getContentAsByteArray(), StandardCharsets.UTF_8);

            log.info(String.format(
                    "API=%s | Method=%s | Status=%d | Duration=%d ms | Request=%s | Response=%s",
                    request.getRequestURI(),
                    request.getMethod(),
                    response.getStatus(),
                    duration,
                    reqBody.trim(),
                    resBody
            ));

            wrappedResponse.copyBodyToResponse();

        }

    }

}
