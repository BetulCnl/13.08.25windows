package com.betul.productmanagement.controller;

import com.betul.productmanagement.dto.AuthResponse;
import com.betul.productmanagement.dto.LoginRequest;
import com.betul.productmanagement.dto.RegisterRequest;
import com.betul.productmanagement.entity.User;
import com.betul.productmanagement.security.JwtUtil;
import com.betul.productmanagement.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import com.betul.productmanagement.dto.RestResponseDTO;
import com.betul.productmanagement.dto.UserResponseDTO;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager; //email şifre eşleşmesi kontrolü
    private final JwtUtil jwtUtil; //token oluşturma doğrulama


    //-----------------------------------------------------------
    //bu alttaki kısımda önceden saçma olarak user nesnesi controller manuel oluşturuyordu, şimdi RegisterRequest alıyor
    @PostMapping("/register")
    public ResponseEntity<RestResponseDTO<UserResponseDTO>> registerUser(
            @Valid @RequestBody RegisterRequest request) {

        try {
            // Servis artık UserResponseDTO döndürüyor
            UserResponseDTO dto = userService.registerUser(request);

            // RestResponse içine sarıyoruz ve tarih ekliyoruz
            RestResponseDTO<UserResponseDTO> response = new RestResponseDTO<>(
                    true,
                    "User registered successfully",
                    dto,
                    LocalDateTime.now()  // Tarih burada ekleniyor
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            RestResponseDTO<UserResponseDTO> response = new RestResponseDTO<>(
                    false,
                    e.getMessage(),
                    null,
                    LocalDateTime.now()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    //-------------------------------------------------------------------------------------------------
    @PostMapping("/login")
    public ResponseEntity<RestResponseDTO<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        try {
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());
            authenticationManager.authenticate(authToken);

            String token = jwtUtil.generateToken(request.getEmail());

            RestResponseDTO<AuthResponse> response = new RestResponseDTO<>(
                    true,
                    "Login successful",
                    new AuthResponse(token),
                    LocalDateTime.now()
            );

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            RestResponseDTO<AuthResponse> response = new RestResponseDTO<>(
                    false,
                    "Invalid email or password",
                    null,
                    LocalDateTime.now()
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
}
