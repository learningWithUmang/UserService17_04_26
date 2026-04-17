package dev.umang.userauthservice_17_04_2026.controllers;

import dev.umang.userauthservice_17_04_2026.dtos.LoginRequestDTO;
import dev.umang.userauthservice_17_04_2026.dtos.SignupRequestDTO;
import dev.umang.userauthservice_17_04_2026.dtos.UserDTO;
import dev.umang.userauthservice_17_04_2026.models.User;
import dev.umang.userauthservice_17_04_2026.services.IAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
1. Signup API
    - Type of API:POST
    - Request : SignupRequestDTO
        -name, email, password
    - Response: UserDTO
        -name, email, roles, etc
2. Login API
    - Type of API: POST
    - Request : LoginRequestDTO
        -email, password
    - Response : UserDTO
        -name, email, roles, etc (token can be included in response header)
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private IAuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<UserDTO> signup(@RequestBody SignupRequestDTO signupRequestDTO) {
        // Logic to handle user signup
        try{
            User user = authService.signup(
                    signupRequestDTO.getName(),
                    signupRequestDTO.getEmail(),
                    signupRequestDTO.getPassword());

            return new ResponseEntity<>(user.convertToUserDTO(), HttpStatus.CREATED);
        } catch (Exception e){
            throw e;
        }
    }

    @PostMapping("/login")
    public ResponseEntity<UserDTO> login(@RequestBody LoginRequestDTO loginRequestDTO) {
        // Logic to handle user login
        try{
            User user = authService.login(
                    loginRequestDTO.getEmail(),
                    loginRequestDTO.getPassword());
            return new ResponseEntity<>(user.convertToUserDTO(), HttpStatus.OK);
        } catch (Exception e){
            throw e;
        }
    }
}

/*
Response - UserDTO
    {
    "id": 1,
    "name": "John Doe",
    ...
    }
 */
