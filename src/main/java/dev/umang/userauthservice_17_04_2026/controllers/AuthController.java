package dev.umang.userauthservice_17_04_2026.controllers;

import dev.umang.userauthservice_17_04_2026.dtos.*;
import dev.umang.userauthservice_17_04_2026.exceptions.UnauthorizedException;
import dev.umang.userauthservice_17_04_2026.models.User;
import dev.umang.userauthservice_17_04_2026.services.IAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

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
            UserToken userToken = authService.login(
                    loginRequestDTO.getEmail(),
                    loginRequestDTO.getPassword());

            //Setting the headers
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.SET_COOKIE, userToken.getToken());

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .body(userToken.getUser().convertToUserDTO());
        } catch (Exception e){
            throw e;
        }
    }

    @PostMapping("/validate-token")
    public void validateToken(@RequestBody ValidateTokenDto validateTokenDto){
        Boolean isValid = authService.validateToken(validateTokenDto.getToken());
        if(!isValid){
            throw new UnauthorizedException("Invalid token");
        }
    }

    @GetMapping("/user/{id}")
    public UserDTO getUserById(@PathVariable Long id) {
        System.out.println("Getting user by id: " + id);
        User user = authService.getUserById(id);
        return user.convertToUserDTO();
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
