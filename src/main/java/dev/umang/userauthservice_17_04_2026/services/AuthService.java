package dev.umang.userauthservice_17_04_2026.services;

import dev.umang.userauthservice_17_04_2026.clients.KafkaProducerHelperClient;
import dev.umang.userauthservice_17_04_2026.dtos.EmailDTO;
import dev.umang.userauthservice_17_04_2026.dtos.UserToken;
import dev.umang.userauthservice_17_04_2026.exceptions.IncorrectPasswordException;
import dev.umang.userauthservice_17_04_2026.exceptions.UserAlreadyExistException;
import dev.umang.userauthservice_17_04_2026.exceptions.UserNotExistException;
import dev.umang.userauthservice_17_04_2026.models.Role;
import dev.umang.userauthservice_17_04_2026.models.Session;
import dev.umang.userauthservice_17_04_2026.models.State;
import dev.umang.userauthservice_17_04_2026.models.User;
import dev.umang.userauthservice_17_04_2026.repositories.RoleRepo;
import dev.umang.userauthservice_17_04_2026.repositories.SessionRepo;
import dev.umang.userauthservice_17_04_2026.repositories.UserRepo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.SecretKey;
import java.util.*;

@Service
public class AuthService implements IAuthService{
    @Autowired
    private UserRepo userRepo;

    @Autowired
    private RoleRepo roleRepo;

    @Autowired
    private SecretKey secretKey;

    @Autowired
    private SessionRepo sessionRepo;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private KafkaProducerHelperClient kafkaProducerClient;

    @Autowired
    private ObjectMapper objectMapper;
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
        user.setPasswordHash(bCryptPasswordEncoder.encode(password));
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

        /*
        Send message to Kafka

        {
            "eventType":"USER_SIGNUP",
            "recipient": <email>,
        }
         */

        EmailDTO emailDTO = new EmailDTO();
        emailDTO.setTo(email);
        emailDTO.setFrom("umangonwork@gmail.com");
        emailDTO.setSubject("Welcome to our service");
        emailDTO.setBody("Hi " + name + ", welcome to our service. We are glad to have you on board.");
        /*
        Convert the message to a string
         */

        kafkaProducerClient.sendMessage(
                "USER_SIGNUP",
                objectMapper.writeValueAsString(emailDTO));
        /*
        {
            "from":email,
            "to": email,
            "subject": "Welcome to our service",
            "body": "Hi " + name + ", welcome to our service. We are glad to have you on board."
        }
         */

        return userRepo.save(user);
    }

    @Override
    public UserToken login(String email, String password) {
        /*
        encode the password, then compare with the password hash stored in the database for the user with the given email. If they match, authentication is successful; otherwise, it fails.

        1. Salt and cost factor
        1. extracts
        encode the password again
         */

        Optional<User> optionalUser = userRepo.findByEmail(email);
        if(optionalUser.isEmpty()){
            throw new UserNotExistException("User with email " + email + " does not exist");
        }

        User user = optionalUser.get();

        if(bCryptPasswordEncoder.matches(password, user.getPasswordHash())){
            /*
            Generate JWT token and send it in response header
             */

            Map<String,Object> payload = new HashMap<>();
            Long nowInMillis = System.currentTimeMillis(); // gets us timestamp in epoch
            payload.put("iat",nowInMillis);
            payload.put("exp",nowInMillis+100000);
            payload.put("userId",user.getId());
            payload.put("iss","scaler");
            payload.put("scope",user.getRoles());
            //Payload generated

            //MacAlgorithm macAlgorithm = Jwts.SIG.HS256;
            //SecretKey secretKey = macAlgorithm.key().build();
            String token = Jwts.builder().claims(payload).signWith(secretKey).compact();

            /*
            Store the session's info in the db (source of truth for all generated tokens)
             */
            Session session = new Session();
            session.setUser(user);
            session.setToken(token);
            session.setState(State.ACTIVE);

            sessionRepo.save(session);

            return new UserToken(user, token);
        } else {
            throw new IncorrectPasswordException("Incorrect password for user with email " + email);
        }
    }

    public Boolean validateToken(String token){

        Optional<Session> optionalSession = sessionRepo.findByToken(token);
        if(optionalSession.isEmpty()){
            return false;
        }

        // Parsing: If the signature has been tampered with, the parser will throw an exception.
        //Environment variables (within Ms)
        //Config service -- shared across multiple services

        JwtParser jwtParser = Jwts.parser().verifyWith(secretKey).build();
        Claims claims = jwtParser.parseSignedClaims(token).getPayload(); //A.B.C

        /*
        Internally
        A(headers).B(payload).C(signature)

        signing the payload using the secret key passed in the parser
         */

        Long currentTimeInMills = System.currentTimeMillis();
        Long expiryTime = (Long) claims.get("exp");

        if(expiryTime < currentTimeInMills){
            //expired
            Session session = optionalSession.get();
            session.setState(State.INACTIVE);
            sessionRepo.save(session);
            return false;
        }else{
            return true;
        }

        /*
        Token which is signed
        a.b.c
        claim
        signed claim - signed using claim and secret key as an input

         */

        /*
        Claims has all key user info
         */


    }

    public User getUserById(Long id){
        Optional<User> optionalUser = userRepo.findById(id);
        if(optionalUser.isEmpty()){
            throw new UserNotExistException("User with id " + id + " does not exist");
        }

        return optionalUser.get();
    }

    /*
    JWT
    Parts-3

    Payload = Claims

    While you can use any keys, following JWT standards is recommended:

    iat (Issued At): Timestamp of when the token was created.
    exp (Expiry): When the token becomes invalid.
    iss (Issuer): Who created the token (e.g., "scaler").
    userId: Custom claim for identification.
    scope: Roles/Permissions assigned to the user.
     */

    /*
    Next class we will discuss about tokens and implement JWTs
     */
}
/*
Theory:-

We will use BCryptPasswordEncoder for encoding them. BCryptPasswordEncoder is a Spring Security utility used to hash (encode) passwords securely before storing them and to verify passwords during login.

BCrypt is preferred because it uses strong one-way hashing (cannot be reversed) and automatically adds a random salt.

1. One-Way Hashing: Once the hash is created, it cannot be reversed to get the original value back.
2. Salting: BCrypt automatically adds a random salt.
        Analogy: Think of salt as adding one more layer of security on your hashed value to prevent pre-computed attacks (rainbow tables).
3. Cost Factor: This refers to the number of hashing rounds. More rounds = more secure, but more CPU intensive.

1. Hashing, encoding, encryption

java doesn't even run on OS kernel

Java runs on JVM
 */