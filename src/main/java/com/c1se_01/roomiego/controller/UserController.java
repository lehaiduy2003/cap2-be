package com.c1se_01.roomiego.controller;

import com.c1se_01.roomiego.dto.UserDto;
import com.c1se_01.roomiego.dto.VerificationDto;
import com.c1se_01.roomiego.model.User;
import com.c1se_01.roomiego.service.impl.AuthenticationService;
import com.c1se_01.roomiego.service.VerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RequestMapping("")
@RestController
public class UserController {
    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private VerificationService verificationService;

    @PostMapping("/auth/register")
    public ResponseEntity<UserDto> register(@RequestBody UserDto reg) {
        return ResponseEntity.ok(authenticationService.register(reg));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<UserDto> login(@RequestBody UserDto req) {
        return ResponseEntity.ok(authenticationService.login(req));
    }

    @PostMapping("/auth/refresh")
    public ResponseEntity<UserDto> refreshToken(@RequestBody UserDto req) {
        return ResponseEntity.ok(authenticationService.refreshToken(req));
    }

    @GetMapping("/owner/get-all-users")
    public ResponseEntity<UserDto> getAllUsers() {
        return ResponseEntity.ok(authenticationService.getAllUsers());

    }

    @GetMapping("/owner/get-users/{userId}")
    public ResponseEntity<UserDto> getUSerByID(@PathVariable long userId) {
        return ResponseEntity.ok(authenticationService.getUsersById(userId));

    }

    @GetMapping("/users/email/{email}")
    public ResponseEntity<UserDto> getUserByEmail(@PathVariable String email) {
        UserDto response = authenticationService.getUserByEmail(email);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PutMapping("/owner/update/{userId}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Integer userId, @RequestBody User reqres) {
        return ResponseEntity.ok(authenticationService.updateUser(userId, reqres));
    }

    @GetMapping("/renterowner/get-profile")
    public ResponseEntity<UserDto> getMyProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        UserDto response = authenticationService.getMyInfo(email);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @DeleteMapping("/owner/delete/{userId}")
    public ResponseEntity<UserDto> deleteUSer(@PathVariable long userId) {
        return ResponseEntity.ok(authenticationService.deleteUser(userId));
    }

    @PostMapping("/users/verify-citizen-id")
    public ResponseEntity<VerificationDto> verifyCitizenId(@RequestBody VerificationDto verificationDto) {
        VerificationDto response = verificationService.verifyCitizenId(verificationDto);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/users/verification-status/{userId}")
    public ResponseEntity<VerificationDto> getVerificationStatus(@PathVariable Long userId) {
        VerificationDto response = verificationService.getVerificationStatus(userId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PutMapping("/owner/update-verification/{userId}")
    public ResponseEntity<VerificationDto> updateVerificationStatus(
            @PathVariable Long userId,
            @RequestParam Boolean isVerified) {
        VerificationDto response = verificationService.updateVerificationStatus(userId, isVerified);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/users/verify-citizen-id-with-fptai")
    public ResponseEntity<VerificationDto> verifyWithFptAi(@RequestBody VerificationDto verificationDto) {
        VerificationDto response = verificationService.verifyWithFptAi(verificationDto);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

}
