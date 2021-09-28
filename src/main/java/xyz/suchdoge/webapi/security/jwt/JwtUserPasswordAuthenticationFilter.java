package xyz.suchdoge.webapi.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.crypto.SecretKey;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Date;

public class JwtUserPasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;
    private final SecretKey secretKey;
    private final JwtConfig jwtConfig;

    public JwtUserPasswordAuthenticationFilter(AuthenticationManager authenticationManager,
                                               SecretKey secretKey,
                                               JwtConfig jwtConfig) {
        this.authenticationManager = authenticationManager;
        this.secretKey = secretKey;
        this.jwtConfig = jwtConfig;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        try {
            UsernamePasswordAuthenticationRequest authenticationRequest = new ObjectMapper().readValue(
                    request.getInputStream(),
                    UsernamePasswordAuthenticationRequest.class
            );

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    authenticationRequest.getUsername(),
                    authenticationRequest.getPassword()
            );

            return authenticationManager.authenticate(authentication);
        }
        catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {
        String token = Jwts.builder()
                .setSubject(authResult.getName())
                .claim("authorities", authResult.getAuthorities())
                .setIssuedAt(new Date())
                .setExpiration(java.sql.Date.valueOf(LocalDate.now().plusDays(jwtConfig.getTokenExpirationDays())))
                .signWith(secretKey)
                .compact();


        response.setHeader("Access-Control-Expose-Headers", jwtConfig.getAuthorizationHeader());
        response.addHeader(jwtConfig.getAuthorizationHeader(), jwtConfig.getTokenPrefix() + token);
    }
}
