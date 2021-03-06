package fr.vdelll.springmvc.security;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	// Utilisation de la base de donnée de l'application
	@Autowired
	private DataSource dataSource;

	/**
	 * Permet de dire à spring où aller chercher les utilisateurs
	 */
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {

		PasswordEncoder passwordEncoder = passwordEncoder();

		// Définition d'utilisateurs en mémoire
		// "{noop}mdp" permet de définir qu'on utilise pas d'encodeur (Bcrypt est
		// utilisé par défaut)
//		auth.inMemoryAuthentication().withUser("user1").password(passwordEncoder.encode("test")).roles("USER");
//		auth.inMemoryAuthentication().withUser("user2").password(passwordEncoder.encode("1234")).roles("USER");
//		auth.inMemoryAuthentication().withUser("admin").password(passwordEncoder.encode("1234")).roles("ADMIN", "USER");

		// Authentification avec JDBC
		auth.jdbcAuthentication()
				.dataSource(dataSource)
				.usersByUsernameQuery("SELECT username AS principal, password AS credentials, active FROM users WHERE username=?")
				.authoritiesByUsernameQuery("SELECT username AS principal, role AS role FROM users_roles WHERE username=?")
				.passwordEncoder(passwordEncoder)
				.rolePrefix("ROLE_");
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {

		// Utilisation d'un formulaire d'authentification
		http.formLogin().loginPage("/login");
		// http.httpBasic(); // authentification de http sous forme de pop up

		// Limitation de certaines requêtes
		http.authorizeRequests().antMatchers("/admin/**", "/save**/**", "/delete**/**", "/form**/**").hasRole("ADMIN");
		http.authorizeRequests().antMatchers("/patients**/**").hasRole("USER");
		// Toutes les ressources sont accessible pour les url contenant /user/**
		http.authorizeRequests().antMatchers("/user/**", "/connect", "/webjars/**", "/login").permitAll();
		
		// Toutes les requetes HTTP nécessitent de passer par une authentification
		http.authorizeRequests().anyRequest().authenticated();

		// Appliqué par défaut
		http.csrf();

		// Redirige vers la page notAuthorized si erreur 403
		http.exceptionHandling().accessDeniedPage("/notAuthorized");

	}

	/**
	 * Dans le contexte d'application
	 * 
	 * @return
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}
