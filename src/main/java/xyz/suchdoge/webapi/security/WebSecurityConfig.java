package xyz.suchdoge.webapi.security;

import com.google.common.collect.ImmutableList;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import xyz.suchdoge.webapi.security.jwt.JwtConfig;
import xyz.suchdoge.webapi.security.jwt.JwtTokenVerifier;
import xyz.suchdoge.webapi.security.jwt.JwtUserPasswordAuthenticationFilter;

import javax.crypto.SecretKey;

@EnableWebSecurity
@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;
    private final SecretKey secretKey;
    private final JwtConfig jwtConfig;

    public WebSecurityConfig(PasswordEncoder passwordEncoder,
                             UserDetailsService userDetailsService,
                             SecretKey secretKey,
                             JwtConfig jwtConfig) {
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
        this.secretKey = secretKey;
        this.jwtConfig = jwtConfig;
    }


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .cors()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .addFilter(new JwtUserPasswordAuthenticationFilter(authenticationManager(), secretKey, jwtConfig))
                .addFilterAfter(new JwtTokenVerifier(secretKey, jwtConfig), JwtUserPasswordAuthenticationFilter.class)
                .authorizeRequests()
                .antMatchers("/login", "/register", "/activate/*", "/meme/public/**").permitAll()
                .anyRequest().authenticated();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(daoAuthenticationProvider());
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder);
        provider.setUserDetailsService(userDetailsService);
        return provider;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(ImmutableList.of(CorsConfiguration.ALL));
        configuration.setAllowedMethods(ImmutableList.of("HEAD", "GET", "POST", "PUT", "DELETE", "PATCH"));
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(ImmutableList.of("Authorization", "Cache-Control", "Content-Type"));
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
