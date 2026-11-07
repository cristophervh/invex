package com.invex.employees.logging;

import java.io.IOException;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Logs one line per request with method, path, status, duration and headers.
 * Credentials are masked and bodies are never logged because they carry personal data.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    public static final String REQUEST_ID_KEY = "requestId";

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private static final Set<String> SENSITIVE_HEADERS = Set.of("authorization", "cookie", "proxy-authorization");
    private static final Pattern SAFE_REQUEST_ID = Pattern.compile("^[A-Za-z0-9-]{1,64}$");
    private static final String MASK = "***";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String requestId = resolveRequestId(request);
        MDC.put(REQUEST_ID_KEY, requestId);
        response.setHeader(REQUEST_ID_HEADER, requestId);
        long start = System.currentTimeMillis();
        try {
            chain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - start;
            log.info("{} {} -> {} ({} ms) headers={}", request.getMethod(), request.getRequestURI(),
                    response.getStatus(), duration, maskedHeaders(request));
            MDC.remove(REQUEST_ID_KEY);
        }
    }

    // A client-supplied id is reused only if it is safe to write into the logs
    private String resolveRequestId(HttpServletRequest request) {
        String header = request.getHeader(REQUEST_ID_HEADER);
        if (header != null && SAFE_REQUEST_ID.matcher(header).matches()) {
            return header;
        }
        return UUID.randomUUID().toString();
    }

    private Map<String, String> maskedHeaders(HttpServletRequest request) {
        Map<String, String> headers = new TreeMap<>();
        for (String name : Collections.list(request.getHeaderNames())) {
            String value = SENSITIVE_HEADERS.contains(name.toLowerCase(Locale.ROOT)) ? MASK : request.getHeader(name);
            headers.put(name, value);
        }
        return headers;
    }
}
