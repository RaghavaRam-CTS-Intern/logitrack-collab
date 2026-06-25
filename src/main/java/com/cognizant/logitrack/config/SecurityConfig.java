package com.cognizant.logitrack.config;
 
import com.cognizant.logitrack.security.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
 
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
 
    private final JwtFilter jwtFilter;
    private final UrlBasedCorsConfigurationSource corsConfigurationSource;
 
    public SecurityConfig(JwtFilter jwtFilter, UrlBasedCorsConfigurationSource corsConfigurationSource) {
        this.jwtFilter = jwtFilter;
        this.corsConfigurationSource = corsConfigurationSource;
    }
 
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // --- Public: login only; register requires Admin ---
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/register").hasRole("ADMIN")
                        // Internal audit-log write hook used by other modules (IAM-004)
                        .requestMatchers(HttpMethod.POST, "/api/audit-logs").permitAll()
 
                        // --- IAM: User management (Admin only) ---
                        .requestMatchers("/api/users/**").hasRole("ADMIN")
 
                        // --- Analytics & Reporting (Operations Analyst) ---
                        .requestMatchers(HttpMethod.GET, "/api/audit-logs/**").hasAnyRole("ANALYST", "ADMIN")
                        .requestMatchers("/api/logistics-reports/**").hasAnyRole("ANALYST", "ADMIN")
 
                        // --- Shipment & Freight ---
                        .requestMatchers(HttpMethod.POST, "/api/freight-orders").hasAnyRole("SHIPPER", "COORDINATOR", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/freight-orders/*/cancel").hasAnyRole("COORDINATOR", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/freight-orders/**").hasAnyRole("SHIPPER", "COORDINATOR", "ANALYST", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/shipments").hasAnyRole("COORDINATOR", "ADMIN","SHIPPER")
                        .requestMatchers(HttpMethod.PATCH, "/api/shipments/*/status").hasAnyRole("COORDINATOR", "DRIVER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/shipments/*/events").hasAnyRole("DRIVER", "COORDINATOR", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/shipments/**").hasAnyRole("SHIPPER", "COORDINATOR", "DRIVER", "ANALYST", "ADMIN")
 
                        // --- Route, Carrier & Rate Card (Admin config; Coordinator may read) ---
                        .requestMatchers(HttpMethod.GET, "/api/routes/**").hasAnyRole("COORDINATOR", "ADMIN")
                        .requestMatchers("/api/routes/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/carriers/**").hasAnyRole("COORDINATOR", "ADMIN")
                        .requestMatchers("/api/carriers/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/rate-cards/**").hasAnyRole("COORDINATOR", "ADMIN")
                        .requestMatchers("/api/rate-cards/**").hasRole("ADMIN")
 
                        // --- Warehouse & Inventory Operations ---
                        .requestMatchers("/api/inventory/**").hasAnyRole("WAREHOUSEOPS", "ADMIN")
                        .requestMatchers("/api/inbound-receipts/**").hasAnyRole("WAREHOUSEOPS", "ADMIN")
                        .requestMatchers("/api/pick-lists/**").hasAnyRole("WAREHOUSEOPS", "COORDINATOR", "ADMIN")
 
                        // --- Supplier & Purchase Orders ---
                        .requestMatchers("/api/suppliers/**").hasAnyRole("ADMIN", "COORDINATOR")
                        .requestMatchers("/api/purchase-orders/**").hasAnyRole("ADMIN", "COORDINATOR")
 
                        // --- Compliance & Documentation ---
                        .requestMatchers("/api/shipment-documents/**").hasAnyRole("COMPLIANCE", "ADMIN")
                        .requestMatchers("/api/compliance-flags/**").hasAnyRole("COMPLIANCE", "ADMIN")
 
                        // --- Notifications: any authenticated user ---
                        .requestMatchers("/api/notifications/**").authenticated()
 
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
 
        return http.build();
    }
 
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}