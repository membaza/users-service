package com.membaza.api.users.service.jwt;

import com.membaza.api.users.persistence.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Date;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static javax.xml.bind.DatatypeConverter.parseBase64Binary;

/**
 * @author Emil Forslund
 * @since  1.0.0
 */
@Service
public final class JwtServiceImpl implements JwtService {

    private final Environment env;

    public JwtServiceImpl(Environment env) {
        this.env = requireNonNull(env);
    }

    @Override
    public JwtToken createLoginToken(User user) {
        return createJWT(
            UUID.randomUUID().toString(),
            env.getProperty("service.jws.issuer"),
            user.getId(),
            user.getRoles().stream()
                .flatMap(r -> r.getPrivileges().stream())
                .distinct()
                .collect(joining(",")),
            Long.parseLong(env.getProperty("service.jws.expiration"))
        );
    }

    @Override
    public Claims validate(JwtToken token) {
        return Jwts.parser()
            .setSigningKey(parseBase64Binary(getSecret()))
            .parseClaimsJws(token.getToken())
            .getBody();
    }

    private JwtToken createJWT(String id,
                               String issuer,
                               String subject,
                               String privileges,
                               long ttlMillis) {

        // The JWT signature algorithm we will be using to sign the token
        final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

        final long nowMillis = System.currentTimeMillis();
        final Date now = new Date(nowMillis);

        // We will sign our JWT with our ApiKey secret
        final byte[] apiKeySecretBytes = parseBase64Binary(getSecret());

        final Key signingKey = new SecretKeySpec(
            apiKeySecretBytes,
            signatureAlgorithm.getJcaName()
        );

        // Let's set the JWT Claims
        final JwtBuilder builder = Jwts.builder()
            .setId(id)
            .setIssuer(issuer)
            .setIssuedAt(now)
            .setSubject(subject)
            .setPayload(privileges)
            .signWith(signatureAlgorithm, signingKey);

        // If it has been specified, let's add the expiration
        if (ttlMillis >= 0) {
            long expMillis = nowMillis + ttlMillis;
            Date exp = new Date(expMillis);
            builder.setExpiration(exp);
        }

        // Builds the JWT and serializes it to a compact, URL-safe string
        return new JwtToken(builder.compact());
    }

    private String getSecret() {
        return env.getProperty("service.jws.secret");
    }
}