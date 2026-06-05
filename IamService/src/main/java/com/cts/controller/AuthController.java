package com.cts.controller;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cts.dto.CitizenResponseDto;
import com.cts.dto.ForgotPasswordRequest;
import com.cts.dto.ForgotUsernameRequest;
import com.cts.dto.LoginRequest;
import com.cts.dto.LoginResponse;
import com.cts.dto.RegisterRequest;
import com.cts.dto.ResetPasswordRequest;
import com.cts.dto.UserResponseDTO;
import com.cts.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/register")
	public ResponseEntity<Map<String, String>> register(@RequestBody RegisterRequest request) {
		authService.register(request);
		return ResponseEntity.ok(Map.of("message","User registered successfully"));
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/admin/register")
	public ResponseEntity<?> adminRegisterUser(@RequestBody RegisterRequest request) {
		authService.adminRegisterUser(request);
		return ResponseEntity.ok(Map.of("message","User registered successfully"));
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
		LoginResponse response = authService.login(request);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/forgot-username")
	public ResponseEntity<?> forgotUsername(@RequestBody ForgotUsernameRequest request) {
		String username = authService.forgotUsername(request);
		return ResponseEntity.ok(Map.of("message","Your username is: " + username));
	}

	@PostMapping("/forgot-password")
	public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
		authService.generateResetToken(request.getUsername());
		return ResponseEntity.ok(Map.of("message","Password reset token generated. Check console."));
	}

	@PostMapping("/reset-password")
	public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
		authService.resetPassword(request);
		return ResponseEntity.ok(Map.of("message","Password reset successful"));
	}

	@GetMapping("/citizens/{id}")
	public ResponseEntity<CitizenResponseDto> getCitizenById(@PathVariable Long id) {
		return ResponseEntity.ok(authService.getCitizenById(id));
	}
	
	@PreAuthorize("hasRole('SUPERVISOR')")
	@GetMapping("/users/workers/{id}")
	public ResponseEntity<UserResponseDTO> getWorkerById(@PathVariable Long id) {
		return ResponseEntity.ok(authService.getWorkerById(id));
	}

	@PreAuthorize("hasAnyRole('SUPERVISOR', 'ADMIN')")
	@GetMapping("/users/workers/active")
	public ResponseEntity<List<UserResponseDTO>> getAllActiveWorkers() {
		return ResponseEntity.ok(authService.getAllActiveWorkers());
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/users")
	public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
		return ResponseEntity.ok(authService.getAllUsers());
	}
	
	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/users/{id}")
	public ResponseEntity<UserResponseDTO> getUserResponseDTOById(@PathVariable Long id) {
		return ResponseEntity.ok(authService.getUserResponseDTOById(id));
	}
	
	@PreAuthorize("hasRole('ADMIN')")
	@PutMapping("/users/{id}/status")
	public ResponseEntity<UserResponseDTO> changeStatusUser(@PathVariable Long id,@RequestParam String status) {
		return ResponseEntity.ok(authService.changeStatusUser(id,status));
	}
}