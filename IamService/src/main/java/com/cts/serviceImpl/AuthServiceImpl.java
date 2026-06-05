package com.cts.serviceImpl;

import java.util.List;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cts.dto.CitizenResponseDto;
import com.cts.dto.ForgotUsernameRequest;
import com.cts.dto.LoginRequest;
import com.cts.dto.LoginResponse;
import com.cts.dto.RegisterRequest;
import com.cts.dto.ResetPasswordRequest;
import com.cts.dto.UserResponseDTO;
import com.cts.entity.User;
import com.cts.exception.AccountDeactivatedException;
import com.cts.exception.EmailAlreadyExistsException;
import com.cts.exception.InvalidCredentialsException;
import com.cts.exception.UserNotFoundException;
import com.cts.exception.UsernameAlreadyExistsException;
import com.cts.repository.UserRepository;
import com.cts.security.JwtUtil;
import com.cts.service.AuthService;

@Service
public class AuthServiceImpl implements AuthService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    public AuthServiceImpl(UserRepository userRepository,PasswordEncoder passwordEncoder,JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public void register(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new UsernameAlreadyExistsException("Username is already taken");
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException("Email is already registered");
        }

        User user = new User();
        user.setName(request.getName());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail()); 
        
        user.setRole("CITIZEN"); 
        user.setStatus("ACTIVE");
        
        userRepository.save(user);
    }
    
    public void adminRegisterUser(RegisterRequest request) {
    	if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new UsernameAlreadyExistsException("Username is already taken");
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email is already registered");
        }

        User user = new User();
        user.setName(request.getName());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail()); 
        
        user.setRole(request.getRole()); 
        user.setStatus("ACTIVE");
        
        userRepository.save(user);
    	
    }

    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));


        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }
        if(user.getStatus().equals("INACTIVE")) {
        	throw new AccountDeactivatedException("Your Account has been Deactivated by Admin");
        }

        
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole(),user.getUserId());
        return new LoginResponse(token, user.getRole(),user.getUserId());
    }

    
    public String forgotUsername(ForgotUsernameRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found for this email address"));

        return user.getUsername();
    }

    
    public void generateResetToken(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if ("ADMIN".equalsIgnoreCase(user.getRole()) || "SUPERVISOR".equalsIgnoreCase(user.getRole())) {
            throw new RuntimeException("Not allowed to reset system user passwords");
        }

        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        userRepository.save(user);

        System.out.println("Password Reset Token for " + username + ": " + token);
    }

    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByResetToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Invalid or expired token"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetToken(null);
        userRepository.save(user);
    }
   
    public User getUserById(Long id) {
    	return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("No User Found on "+id));
    }

	@Override
	public CitizenResponseDto getCitizenById(Long citizenId) {
		User user = getUserById(citizenId);
		
		CitizenResponseDto dto = new CitizenResponseDto();
		dto.setUserId(user.getUserId());
		dto.setEmail(user.getEmail());
		dto.setName(user.getName());
		dto.setUsername(user.getUsername());
		dto.setStatus(user.getStatus());
		dto.setRole(user.getRole());
		return dto;
	}

	@Override
	public UserResponseDTO getWorkerById(Long id) {
		User user = userRepository.findByUserIdAndRole(id, "WORKER").orElseThrow(() -> new UserNotFoundException("Worker Not Found"));
		UserResponseDTO dto = UserResponseDTO.builder().userId(user.getUserId()).name(user.getName()).role(user.getRole()).status(user.getStatus()).username(user.getUsername()).build();
		return dto;
	}

	@Override
	public List<UserResponseDTO> getAllActiveWorkers() {
		List<UserResponseDTO> dto = userRepository.findByRoleAndStatus("WORKER", "ACTIVE").stream().map((user) -> UserResponseDTO.builder()
				.userId(user.getUserId())
				.name(user.getName())
				.role(user.getRole())
				.status(user.getStatus())
				.username(user.getUsername())
				.build()).toList();
		return dto;
	}

	@Override
	public List<UserResponseDTO> getAllUsers() {
		List<UserResponseDTO> dto = userRepository.findAll().stream().map((user) -> UserResponseDTO.builder()
				.userId(user.getUserId())
				.name(user.getName())
				.role(user.getRole())
				.email(user.getEmail())
				.status(user.getStatus())
				.username(user.getUsername())
				.build()).toList();
		return dto;
	}
	

	@Override
	public UserResponseDTO changeStatusUser(Long id,String status) {
		User user = getUserById(id);
		user.setStatus(status);
		userRepository.save(user);
		return UserResponseDTO.builder()
				.userId(user.getUserId())
				.email(user.getEmail())
				.name(user.getName())
				.username(user.getUsername())
				.role(user.getRole())
				.status(user.getStatus())
				.build();
	}

	@Override
	public UserResponseDTO getUserResponseDTOById(Long id) {
		User user = getUserById(id);
		return UserResponseDTO.builder()
				.userId(id)
				.name(user.getName())
				.username(user.getUsername())
				.role(user.getRole())
				.status(user.getStatus())
				.email(user.getEmail())
				.build();
	}
	
}