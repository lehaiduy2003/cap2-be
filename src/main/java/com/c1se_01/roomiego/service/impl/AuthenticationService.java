package com.c1se_01.roomiego.service.impl;

import com.c1se_01.roomiego.dto.UserDto;
import com.c1se_01.roomiego.enums.Gender;
import com.c1se_01.roomiego.enums.Role;
import com.c1se_01.roomiego.model.User;
import com.c1se_01.roomiego.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;

@Slf4j
@Service
public class AuthenticationService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private PasswordEncoder passwordEncoder;


    public UserDto register(UserDto registrationRequest) {
        UserDto resp = new UserDto();
        try {
            log.info("Received registration request: {}", registrationRequest);

            User ourUser = new User();
            ourUser.setEmail(registrationRequest.getEmail());
            ourUser.setRole(Role.valueOf(registrationRequest.getRole().toUpperCase()));
            ourUser.setGender(Gender.valueOf(registrationRequest.getGender().toUpperCase()));
            ourUser.setFullName(registrationRequest.getFullName());
            ourUser.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
            ourUser.setPhone(registrationRequest.getPhone());
            ourUser.setBio(registrationRequest.getBio());
            
            // Convert String dob to Date
            if (registrationRequest.getDob() != null) {
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Date dob = dateFormat.parse(registrationRequest.getDob());
                    ourUser.setDob(dob);
                } catch (ParseException e) {
                    log.error("Error parsing date: ", e);
                    throw new RuntimeException("Invalid date format. Expected yyyy-MM-dd");
                }
            }

            User ourUsersResult = userRepository.save(ourUser);
            log.info("User saved successfully: {}", ourUsersResult);

            if (ourUsersResult.getId() > 0) {
                resp.setUser(ourUsersResult);
                resp.setMessage("User Saved Successfully");
                resp.setStatusCode(200);
            }
        } catch (Exception e) {
            log.error("Error during registration: ", e);
            resp.setStatusCode(500);
            resp.setError("Lỗi hệ thống: " + e.getMessage());
        }
        return resp;
    }




    public UserDto login(UserDto loginRequest) {
        UserDto response = new UserDto();
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );

            // Lấy user từ database
            User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            // Tạo JWT token
            String jwt = jwtService.generateToken(user);
            String refreshToken = jwtService.generateRefreshToken(new HashMap<>(), user);

            // Gán thông tin vào response
            response.setStatusCode(200);
            response.setToken(jwt);
            response.setRefreshToken(refreshToken);
            response.setExpirationTime("24Hrs");
            response.setMessage("Successfully Logged In");
            
            // Set user information
            response.setId(user.getId());
            response.setEmail(user.getEmail());
            response.setFullName(user.getFullName());
            response.setRole(user.getRole().name());
            response.setPhone(user.getPhone());
            response.setGender(user.getGender().name());
            response.setDob(user.getDob() != null ? user.getDob().toString() : null);
            response.setBio(user.getBio());
            response.setCreatedAt(user.getCreatedAt());

        } catch (Exception e) {
            log.error("Login error: ", e);
            response.setStatusCode(500);
            response.setMessage(e.getMessage());
        }
        return response;
    }





    public UserDto refreshToken(UserDto refreshTokenReqiest){
        UserDto response = new UserDto();
        try{
            String ourEmail = jwtService.extractUsername(refreshTokenReqiest.getToken());
            User users = userRepository.findByEmail(ourEmail).orElseThrow();
            if (jwtService.isTokenValid(refreshTokenReqiest.getToken(), users)) {
                var jwt = jwtService.generateToken(users);
                response.setStatusCode(200);
                response.setToken(jwt);
                response.setRefreshToken(refreshTokenReqiest.getToken());
                response.setExpirationTime("24Hr");
                response.setMessage("Successfully Refreshed Token");
            }
            response.setStatusCode(200);
            return response;

        }catch (Exception e){
            response.setStatusCode(500);
            response.setMessage(e.getMessage());
            return response;
        }
    }


    public UserDto getAllUsers() {
        UserDto reqRes = new UserDto();

        try {
            List<User> result = userRepository.findAll();
            if (!result.isEmpty()) {
                reqRes.setUsersList(result);
                reqRes.setStatusCode(200);
                reqRes.setMessage("Successful");
            } else {
                reqRes.setStatusCode(404);
                reqRes.setMessage("No users found");
            }
            return reqRes;
        } catch (Exception e) {
            reqRes.setStatusCode(500);
            reqRes.setMessage("Error occurred: " + e.getMessage());
            return reqRes;
        }
    }

    public UserDto getUsersById(Long id) {
        UserDto reqRes = new UserDto();
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User Not found"));

            // Create a new User object with only necessary fields
            User userResponse = new User();
            userResponse.setId(user.getId());
            userResponse.setFullName(user.getFullName());
            userResponse.setEmail(user.getEmail());
            userResponse.setPhone(user.getPhone());
            userResponse.setRole(user.getRole());
            userResponse.setGender(user.getGender());
            userResponse.setDob(user.getDob());
            userResponse.setBio(user.getBio());
            userResponse.setCreatedAt(user.getCreatedAt());

            // Set the simplified user object
            reqRes.setUsersList(Collections.singletonList(userResponse));
            reqRes.setStatusCode(200);
            reqRes.setMessage("Users with id '" + id + "' found successfully");

        } catch (Exception e) {
            log.error("Error getting user by id: ", e);
            reqRes.setStatusCode(500);
            reqRes.setMessage("Error occurred: " + e.getMessage());
        }
        return reqRes;
    }



    public UserDto deleteUser(long userId) {
        UserDto reqRes = new UserDto();
        try {
            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isPresent()) {
                userRepository.deleteById(userId);
                reqRes.setStatusCode(200);
                reqRes.setMessage("User deleted successfully");
            } else {
                reqRes.setStatusCode(404);
                reqRes.setMessage("User not found for deletion");
            }
        } catch (Exception e) {
            reqRes.setStatusCode(500);
            reqRes.setMessage("Error occurred while deleting user: " + e.getMessage());
        }
        return reqRes;
    }

    public UserDto updateUser(long userId, User updatedUser) {
        UserDto reqRes = new UserDto();
        try {
            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isPresent()) {
                User existingUser = userOptional.get();
                existingUser.setEmail(updatedUser.getEmail());
                existingUser.setFullName(updatedUser.getFullName());
                existingUser.setRole(updatedUser.getRole());
                existingUser.setPhone(updatedUser.getPhone()); // ✅ Thêm số điện thoại
                existingUser.setBio(updatedUser.getBio());     // ✅ Thêm tiểu sử (bio)
                existingUser.setDob(updatedUser.getDob());    // ✅ Thêm ngày sinh (dob)
                existingUser.setGender(updatedUser.getGender());
                // Check if password is present in the request
                if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
                    // Encode the password and update it
                    existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
                }

                User savedUser = userRepository.save(existingUser);
                reqRes.setUser(savedUser);
                reqRes.setStatusCode(200);
                reqRes.setMessage("User updated successfully");
            } else {
                reqRes.setStatusCode(404);
                reqRes.setMessage("User not found for update");
            }
        } catch (Exception e) {
            reqRes.setStatusCode(500);
            reqRes.setMessage("Error occurred while updating user: " + e.getMessage());
        }
        return reqRes;
    }


    public UserDto getMyInfo(String email) {
        UserDto reqRes = new UserDto();
        try {
            Optional<User> userOptional = userRepository.findByEmail(email);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                
                // Set user information
                reqRes.setId(user.getId());
                reqRes.setEmail(user.getEmail());
                reqRes.setFullName(user.getFullName());
                reqRes.setRole(user.getRole().name());
                reqRes.setPhone(user.getPhone());
                reqRes.setGender(user.getGender().name());
                reqRes.setDob(user.getDob() != null ? user.getDob().toString() : null);
                reqRes.setBio(user.getBio());
                reqRes.setCreatedAt(user.getCreatedAt());
                
                reqRes.setStatusCode(200);
                reqRes.setMessage("successful");
            } else {
                reqRes.setStatusCode(404);
                reqRes.setMessage("User not found");
            }
        } catch (Exception e) {
            log.error("Error getting user info: ", e);
            reqRes.setStatusCode(500);
            reqRes.setMessage("Error occurred while getting user info: " + e.getMessage());
        }
        return reqRes;
    }
}