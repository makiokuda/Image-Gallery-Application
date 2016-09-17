//Modified by Maki Okuda
//Cloud Computing for Business: HW2

package edu.cmu.iccb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@SpringBootApplication
@EnableOAuth2Client
public class Application extends WebSecurityConfigurerAdapter{

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Autowired
    OAuth2ClientContext oauth2ClientContext;

    //Reference: https://github.com/spring-guides/tut-spring-boot-oauth2/tree/master/github
    private OAuth2ClientAuthenticationProcessingFilter ssoFilter() {
    	
        OAuth2ClientAuthenticationProcessingFilter githubFilter 
        		= new OAuth2ClientAuthenticationProcessingFilter("/login/github");
        OAuth2RestTemplate githubTemplate = new OAuth2RestTemplate(github(), oauth2ClientContext);
        githubFilter.setRestTemplate(githubTemplate);
        githubFilter.setTokenServices(new UserInfoTokenServices(githubResource().getUserInfoUri(), github().getClientId()));

        return githubFilter;
    }
    
    //Reference: https://github.com/spring-guides/tut-spring-boot-oauth2/tree/master/github
    @Bean
    @ConfigurationProperties("github.client")
    public AuthorizationCodeResourceDetails github() {
        return new AuthorizationCodeResourceDetails();
    }

    //Reference: https://github.com/spring-guides/tut-spring-boot-oauth2/tree/master/github
    @Bean
    @ConfigurationProperties("github.resource")
    public ResourceServerProperties githubResource() {
        return new ResourceServerProperties();
    }

    //Reference: https://spring.io/guides/tutorials/spring-boot-oauth2/
    @Bean
    public FilterRegistrationBean oauth2ClientFilterRegistration(
            OAuth2ClientContextFilter filter) {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(filter);
        registration.setOrder(-100);
        return registration;
    }
    
    //Reference: https://spring.io/guides/tutorials/spring-boot-oauth2/
    //Reference: http://stackoverflow.com/questions/26964590/spring-security-mapping-for-wildcards
    @Override
    protected void configure(HttpSecurity http) throws Exception { 	
        http.addFilterBefore(ssoFilter(), BasicAuthenticationFilter.class);
        http.antMatcher("/**")
            .authorizeRequests()
                .antMatchers("/","/login/**", "/css/**", "/js/**", "/fonts/**")
                .permitAll()
            .anyRequest()
                .authenticated();
        http.csrf().disable();        
    }

}

