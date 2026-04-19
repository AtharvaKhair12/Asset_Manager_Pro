package com.nam.assetmanager.config;

import com.nam.assetmanager.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        private final CustomUserDetailsService userDetailsService;

        public SecurityConfig(CustomUserDetailsService userDetailsService) {
                this.userDetailsService = userDetailsService;
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf.disable()) // Mandatory for public POST reports
                                .headers(headers -> headers.frameOptions(frame -> frame.disable())) // Enable iframe embedding
                                .authorizeHttpRequests(auth -> auth
                                                /* RULE 1: Whitelist landing, auth, and static resources */
                                                .requestMatchers("/", "/register", "/login", "/employee/register", "/employee/login", "/verify",
                                                                "/css/**", "/js/**", "/images/**", "/error")
                                                .permitAll()

                                                /*
                                                 * RULE 2: String-based whitelisting for public scan paths.
                                                 * This replaces the AntPathRequestMatcher to resolve IDE errors.
                                                 */
                                                .requestMatchers("/public/**").permitAll()

                                                /* RULE 3: Restrict management functions to Admins */
                                                .requestMatchers("/add-asset", "/update-asset", "/delete-asset/**", "/dashboard/export", "/dashboard/search")
                                                .hasRole("ADMIN")

                                                /* RULE 4: Protect all other administrative endpoints */
                                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .defaultSuccessUrl("/dashboard", true)
                                                .permitAll())
                                .logout(logout -> logout
                                                .logoutSuccessUrl("/login?logout")
                                                .permitAll());

                return http.build();
        }

        @Bean
        public AuthenticationManager authManager(HttpSecurity http) throws Exception {
                AuthenticationManagerBuilder authenticationManagerBuilder = http
                                .getSharedObject(AuthenticationManagerBuilder.class);

                authenticationManagerBuilder
                                .userDetailsService(userDetailsService)
                                .passwordEncoder(passwordEncoder());

                return authenticationManagerBuilder.build();
        }
}