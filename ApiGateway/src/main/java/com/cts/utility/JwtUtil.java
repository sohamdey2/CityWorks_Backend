package com.cts.utility;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

	private final String SECRET_KEY = "755e4400ca9b850a43dd026081259da308374e93f7e85a5b3d205036c0d9fd4c";
																											
	private final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

	public String extractUsername(String token) {
		return extractClaims(token).getSubject();
	}

	public String generateToken(String username, String role) {
		return Jwts.builder().setSubject(username).claim("role", role).setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 1))
				.signWith(key, SignatureAlgorithm.HS256).compact();
	}

	public Claims extractClaims(String token) {
		return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
	}

	public String extractRole(String token) {
		return extractClaims(token).get("role", String.class);
	}

	public boolean isTokenValid(String token) {
		try { 
			return !extractClaims(token).getExpiration().before(new Date());
			} catch(JwtException | IllegalArgumentException e) {
				return false;
			}
	}
}
