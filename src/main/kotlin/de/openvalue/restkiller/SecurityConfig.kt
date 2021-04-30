package de.openvalue.restkiller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod.*
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter

@Configuration
class SecurityConfig : WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity) {
        http
            .csrf().disable()
            .authorizeRequests()
            .antMatchers(OPTIONS, "/").permitAll()
            .antMatchers(GET, "/").permitAll()
            .antMatchers(POST, "/").hasRole("ADMIN")
            .antMatchers(PUT, "/").hasRole("USER")
            .antMatchers(DELETE, "/").hasRole("USER")
            .anyRequest().authenticated()
            .and().httpBasic()
    }

    @Autowired
    fun configureGlobal(auth: AuthenticationManagerBuilder) {
        auth.inMemoryAuthentication().withUser("admin").password("{noop}password").roles("ADMIN")
        auth.inMemoryAuthentication().withUser("player1").password("{noop}airuairh276gcusbcwi0").roles("USER")
        auth.inMemoryAuthentication().withUser("player2").password("{noop}akjcnayc7ec82819ue29").roles("USER")
        auth.inMemoryAuthentication().withUser("player3").password("{noop}zanx9qqznvnweffvr9bg").roles("USER")
        auth.inMemoryAuthentication().withUser("player4").password("{noop}acjnduwgvtrtr6e36t33").roles("USER")
    }

}
