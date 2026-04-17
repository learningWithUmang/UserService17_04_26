package dev.umang.userauthservice_17_04_2026.services;

import dev.umang.userauthservice_17_04_2026.exceptions.IncorrectPasswordException;
import dev.umang.userauthservice_17_04_2026.exceptions.UserAlreadyExistException;
import dev.umang.userauthservice_17_04_2026.exceptions.UserNotExistException;
import dev.umang.userauthservice_17_04_2026.models.Role;
import dev.umang.userauthservice_17_04_2026.models.State;
import dev.umang.userauthservice_17_04_2026.models.User;
import dev.umang.userauthservice_17_04_2026.repositories.RoleRepo;
import dev.umang.userauthservice_17_04_2026.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.PatternSyntaxException;

@Service
public class AuthService implements IAuthService{
    @Autowired
    private UserRepo userRepo;

    @Autowired
    private RoleRepo roleRepo;
    @Override
    public User signup(String name, String email, String password) throws UserAlreadyExistException {
        //Check if user with email already exists
        Optional<User> optionalUser = userRepo.findByEmail(email);

        if(optionalUser.isPresent()){
            throw new UserAlreadyExistException("User with email " + email + " already exists");
        }

        User user = new User();
        user.setUsername(name);
        user.setEmail(email);
        user.setPasswordHash(password);
        //user.setCreatedAt(System.currentTimeMillis());
        user.setState(State.ACTIVE);
        //user.setLastUpdatedAt(System.currentTimeMillis());

        //I want to assign a default role to the user
        Optional<Role> optionalRole = roleRepo.findByValue("DEFAULT");
        Role roleToBeSet;

        if(optionalRole.isEmpty()){
            Role role = new Role();
            role.setValue("DEFAULT");
            roleRepo.save(role);
            roleToBeSet = role;
        } else{
            roleToBeSet = optionalRole.get();
        }

        user.setRoles(List.of(roleToBeSet));
        return userRepo.save(user);
    }

    @Override
    public User login(String email, String password) {

        Optional<User> optionalUser = userRepo.findByEmail(email);
        if(optionalUser.isEmpty()){
            throw new UserNotExistException("User with email " + email + " does not exist");
        }

        User user = optionalUser.get();

        if(password.equals(user.getPasswordHash())){
            return user;
        } else {
            throw new IncorrectPasswordException("Incorrect password for user with email " + email);
        }
    }

    /*
    Next class we will discuss about tokens and implement JWTs
     */
}
