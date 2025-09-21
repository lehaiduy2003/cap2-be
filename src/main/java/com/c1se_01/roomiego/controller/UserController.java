package com.c1se_01.roomiego.controller;

import com.c1se_01.roomiego.dto.UserDto;
import com.c1se_01.roomiego.model.User;
import com.c1se_01.roomiego.service.impl.AuthenticationService;
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


    @PostMapping("/auth/register")
    public ResponseEntity<UserDto> register(@RequestBody UserDto reg){
        return ResponseEntity.ok(authenticationService.register(reg));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<UserDto> login(@RequestBody UserDto req){
        return ResponseEntity.ok(authenticationService.login(req));
    }

    @PostMapping("/auth/refresh")
    public ResponseEntity<UserDto> refreshToken(@RequestBody UserDto req){
        return ResponseEntity.ok(authenticationService.refreshToken(req));
    }

    @GetMapping("/owner/get-all-users")
    public ResponseEntity<UserDto> getAllUsers(){
        return ResponseEntity.ok(authenticationService.getAllUsers());

    }

    @GetMapping("/owner/get-users/{userId}")
    public ResponseEntity<UserDto> getUSerByID(@PathVariable long userId){
        return ResponseEntity.ok(authenticationService.getUsersById(userId));

    }

    @PutMapping("/owner/update/{userId}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Integer userId, @RequestBody User reqres){
        return ResponseEntity.ok(authenticationService.updateUser(userId, reqres));
    }

    @GetMapping("/renterowner/get-profile")
    public ResponseEntity<UserDto> getMyProfile(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        UserDto response = authenticationService.getMyInfo(email);
        return  ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @DeleteMapping("/owner/delete/{userId}")
    public ResponseEntity<UserDto> deleteUSer(@PathVariable long userId){
        return ResponseEntity.ok(authenticationService.deleteUser(userId));
    }


}
