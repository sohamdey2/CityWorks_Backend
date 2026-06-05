package com.cts.service;

import java.util.List;

import com.cts.dto.CitizenResponseDto;
import com.cts.dto.ForgotUsernameRequest;
import com.cts.dto.LoginRequest;
import com.cts.dto.LoginResponse;
import com.cts.dto.RegisterRequest;
import com.cts.dto.ResetPasswordRequest;
import com.cts.dto.UserResponseDTO;

public interface AuthService {
	public abstract void register(RegisterRequest request);
	public abstract LoginResponse login(LoginRequest request);
	public abstract String forgotUsername(ForgotUsernameRequest request);
	public abstract void generateResetToken(String username);
	public abstract void resetPassword(ResetPasswordRequest request);
	public abstract void adminRegisterUser(RegisterRequest request);
	CitizenResponseDto getCitizenById(Long citizenId);
	UserResponseDTO getWorkerById(Long id);
	UserResponseDTO getUserResponseDTOById(Long id);
	List<UserResponseDTO> getAllActiveWorkers();
	List<UserResponseDTO> getAllUsers();
	UserResponseDTO changeStatusUser(Long id,String status);

}
