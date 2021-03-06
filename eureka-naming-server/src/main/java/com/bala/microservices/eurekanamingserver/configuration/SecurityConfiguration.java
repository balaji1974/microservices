package com.bala.microservices.eurekanamingserver.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity
@Order(1)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

/*
   @Autowired
   public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
	   String password = passwordEncoder().encode("randompwd");
       auth.inMemoryAuthentication().withUser("balaji")
         .password(password).roles("SYSTEM");
   }
*/
   @Override
   protected void configure(HttpSecurity http) throws Exception {
       http.sessionManagement()
         .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
         .and().requestMatchers().antMatchers("/eureka/**")
         .and().authorizeRequests().antMatchers("/eureka/**")
         .hasRole("SYSTEM").anyRequest().denyAll().and()
         .httpBasic().and().csrf().disable();
   }

	/*
	 * @Bean public BCryptPasswordEncoder passwordEncoder() { return new
	 * BCryptPasswordEncoder(); }
	 * 
	 */

}