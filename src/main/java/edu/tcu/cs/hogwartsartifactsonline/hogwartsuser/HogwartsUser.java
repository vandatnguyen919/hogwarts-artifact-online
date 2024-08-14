package edu.tcu.cs.hogwartsartifactsonline.hogwartsuser;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@Entity
public class HogwartsUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotEmpty(message = "username is required.")
    private String username;

    @NotEmpty(message = "password is required.")
    private String password;

    private boolean enabled;

    @NotEmpty(message = "roles are required.")
    private String roles;
}
