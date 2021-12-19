package xyz.suchdoge.webapi.security.jwt;

import io.jsonwebtoken.JwtException;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import xyz.suchdoge.webapi.exception.DogeHttpException;
import xyz.suchdoge.webapi.service.jwt.JwtService;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtTokenVerifier extends OncePerRequestFilter {
    private final JwtService jwtService;

    public JwtTokenVerifier(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain) throws ServletException, IOException {
        try {
            final Authentication authentication = this.jwtService.getAuthentication(request);
            if (authentication == null) {
                filterChain.doFilter(request, response);
                return;
            }

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (JwtException e) {
            throw new DogeHttpException("Auth token cannot be trusted", HttpStatus.BAD_REQUEST);
        }

        try {
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error");
        }
    }
}
