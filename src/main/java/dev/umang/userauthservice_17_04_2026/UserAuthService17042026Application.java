package dev.umang.userauthservice_17_04_2026;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class UserAuthService17042026Application {

    public static void main(String[] args) {
        SpringApplication.run(UserAuthService17042026Application.class, args);
    }

}
