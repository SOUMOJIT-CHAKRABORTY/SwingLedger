package com.swingledger.app.auth;

import java.util.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/v1/auth")
public class AccountController {
	private final JdbcTemplate db;
	private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

	public AccountController(JdbcTemplate db) {
		this.db = db;
	}

	public record Credentials(String name, String email, String password) {
	}

	@GetMapping("/csrf")
	public Map<String, String> csrf(CsrfToken token) {
		return Map.of("token", token.getToken());
	}

	@PostMapping("/register")
	public Map<String, Object> register(@RequestBody Credentials c, HttpServletRequest request) {
		String email = email(c.email());
		if (c.name() == null || c.name().isBlank() || c.name().length() > 80)
			throw new IllegalArgumentException("Name is required (up to 80 characters)");
		if (c.password() == null || c.password().length() < 10
				|| c.password().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72)
			throw new IllegalArgumentException("Password must have at least 10 characters and at most 72 bytes");
		String id = UUID.randomUUID().toString();
		try {
			db.update("INSERT INTO app_users VALUES (?,?,?,?)", id, email, c.name().trim(),
					encoder.encode(c.password()));
		} catch (DuplicateKeyException e) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
		}
		signIn(id, request);
		return user(id);
	}

	@PostMapping("/login")
	public Map<String, Object> login(@RequestBody Credentials c, HttpServletRequest request) {
		var rows = db.queryForList("SELECT * FROM app_users WHERE email=?", email(c.email()));
		if (rows.isEmpty() || c.password() == null || c.password().length() > 72
				|| !encoder.matches(c.password(), (String) rows.getFirst().get("password_hash")))
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email or password is incorrect");
		String id = (String) rows.getFirst().get("id");
		signIn(id, request);
		return user(id);
	}

	@GetMapping("/me")
	public Map<String, Object> me(java.security.Principal principal) {
		return user(principal.getName());
	}

	@PostMapping("/logout")
	public void logout(HttpServletRequest request) {
		var session = request.getSession(false);
		if (session != null)
			session.invalidate();
		SecurityContextHolder.clearContext();
	}

	private Map<String, Object> user(String id) {
		return db.queryForMap("SELECT id,email,name FROM app_users WHERE id=?", id);
	}

	private String email(String email) {
		if (email == null || email.length() > 254 || !email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))
			throw new IllegalArgumentException("A valid email is required");
		return email.trim().toLowerCase(Locale.ROOT);
	}

	private void signIn(String id, HttpServletRequest request) {
		request.getSession();
		request.changeSessionId();
		var context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(new UsernamePasswordAuthenticationToken(id, null, List.of()));
		SecurityContextHolder.setContext(context);
		request.getSession().setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
	}
}
