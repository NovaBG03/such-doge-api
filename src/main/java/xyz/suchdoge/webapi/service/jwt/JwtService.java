package xyz.suchdoge.webapi.service.jwt;

import com.google.common.base.Strings;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class JwtService {
    private final UserDetailsService userDetailsService;
    private final SecretKey secretKey;
    private final JwtConfig jwtConfig;

    public JwtService(UserDetailsService userDetailsService, SecretKey secretKey, JwtConfig jwtConfig) {
        this.userDetailsService = userDetailsService;
        this.secretKey = secretKey;
        this.jwtConfig = jwtConfig;
    }

    public Authentication getAuthentication(HttpServletRequest request) {
        String authorizationToken = request.getHeader(jwtConfig.getAuthorizationHeader());

        if (Strings.isNullOrEmpty(authorizationToken) || !authorizationToken.startsWith(jwtConfig.getTokenPrefix())) {
            return null;
        }

        String token = authorizationToken.replace(jwtConfig.getTokenPrefix(), "");

        Jws<Claims> claimsJws = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token);

        Claims body = claimsJws.getBody();

        String username = body.getSubject();

        var authorities = (List<Map<String, String>>) body.get("authorities");

        // todo validate jws claims

        Set<SimpleGrantedAuthority> simpleGrantedAuthorities = authorities.stream()
                .map(authorityMap -> new SimpleGrantedAuthority(authorityMap.get("authority")))
                .collect(Collectors.toSet());

        return new UsernamePasswordAuthenticationToken(
                username,
                null,
                simpleGrantedAuthorities
        );
    }

    public String createJwt(String username) {
        final UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("authorities", userDetails.getAuthorities())
                .setIssuedAt(new Date())
                .setExpiration(Date.from(Instant.now().plusSeconds(jwtConfig.getTokenExpirationSeconds())))
                .signWith(secretKey)
                .compact();
    }

    public void createNewAuthorizationResponseHeader(HttpServletResponse response, String principalName) {
        final String jwt = this.createJwt(principalName);
        this.setAuthorizationResponseHeader(response, jwt);
    }

    public void setAuthorizationResponseHeader(HttpServletResponse response, String jwt) {
        // todo add cors header
        response.addHeader("Access-Control-Expose-Headers", jwtConfig.getAuthorizationHeader());
        response.setHeader(jwtConfig.getAuthorizationHeader(), jwtConfig.getTokenPrefix() + jwt);
    }
}
