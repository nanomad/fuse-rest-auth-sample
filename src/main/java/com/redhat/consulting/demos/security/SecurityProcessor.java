package com.redhat.consulting.demos.security;


import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import javax.security.auth.Subject;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;

public class SecurityProcessor implements Processor {

    private static final Map<String, String> STATIC_CREDS = Collections.singletonMap("user", "password");

    @Override
    public void process(Exchange exchange) throws Exception {

        String authorization = exchange.getMessage().getHeader("Authorization", String.class);
        if (authorization != null && authorization.toLowerCase().startsWith("basic")) {
            // Authorization: Basic base64credentials
            String base64Credentials = authorization.substring("Basic".length()).trim();
            byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
            String credentials = new String(credDecoded, StandardCharsets.UTF_8);
            // credentials = username:password
            final String[] values = credentials.split(":", 2);
            boolean loginOk = STATIC_CREDS.getOrDefault(values[0], "").equals(values[1]);
            if (!loginOk) {
                NotAuthenticatedException notAuthenticatedException = new NotAuthenticatedException();
                exchange.setException(notAuthenticatedException);
            } else {
                exchange.getMessage().setHeader(Exchange.AUTHENTICATION, makeSubject(values[0]));
            }
        } else {
            NotAuthenticatedException notAuthenticatedException = new NotAuthenticatedException();
            exchange.setException(notAuthenticatedException);
        }
    }

    private Subject makeSubject(String value) {
        return new Subject(true, Collections.singleton(new SimplePrincipal(value)), Collections.emptySet(), Collections.emptySet());
    }
}
