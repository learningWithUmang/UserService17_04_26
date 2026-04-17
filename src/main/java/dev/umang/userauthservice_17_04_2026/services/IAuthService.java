package dev.umang.userauthservice_17_04_2026.services;

import dev.umang.userauthservice_17_04_2026.exceptions.UserAlreadyExistException;
import dev.umang.userauthservice_17_04_2026.models.User;

public interface IAuthService {
    User signup(String name, String email, String password) throws UserAlreadyExistException;
    User login(String email,String password);

}
