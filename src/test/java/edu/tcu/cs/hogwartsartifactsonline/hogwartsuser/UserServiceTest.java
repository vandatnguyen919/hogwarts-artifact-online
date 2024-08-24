package edu.tcu.cs.hogwartsartifactsonline.hogwartsuser;

import edu.tcu.cs.hogwartsartifactsonline.system.exception.ObjectNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles(value = "dev")
class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    UserService userService;

    List<HogwartsUser> hogwartsUsers;

    @BeforeEach
    void setUp() {
        HogwartsUser u1 = new HogwartsUser();
        u1.setId(1);
        u1.setUsername("john");
        u1.setPassword("123456");
        u1.setEnabled(true);
        u1.setRoles("admin user");

        HogwartsUser u2 = new HogwartsUser();
        u2.setId(2);
        u2.setUsername("eric");
        u2.setPassword("654321");
        u2.setEnabled(true);
        u2.setRoles("user");

        HogwartsUser u3 = new HogwartsUser();
        u3.setId(3);
        u3.setUsername("tom");
        u3.setPassword("qwerty");
        u3.setEnabled(false);
        u3.setRoles("user");

        this.hogwartsUsers = new ArrayList<>();
        this.hogwartsUsers.add(u1);
        this.hogwartsUsers.add(u2);
        this.hogwartsUsers.add(u3);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testFindAllSuccess() {
        // Given
        given(userRepository.findAll()).willReturn(hogwartsUsers);

        // When
        List<HogwartsUser> users = userService.findAll();

        // Then
        assertThat(users.size()).isEqualTo(hogwartsUsers.size());

        verify(this.userRepository, times(1)).findAll();
    }

    @Test
    void testFindByIdSuccess() {
        // Given
        HogwartsUser u = new HogwartsUser();
        u.setId(1);
        u.setUsername("john");
        u.setPassword("123456");
        u.setEnabled(true);
        u.setRoles("admin user");

        given(userRepository.findById(1)).willReturn(Optional.of(u));

        // When
        HogwartsUser user = userService.findById(1);

        // Then
        assertThat(user.getId()).isEqualTo(1);
        assertThat(user.getUsername()).isEqualTo("john");
        assertThat(user.getPassword()).isEqualTo("123456");
        assertThat(user.isEnabled()).isEqualTo(true);
        assertThat(user.getRoles()).isEqualTo("admin user");
        verify(this.userRepository, times(1)).findById(1);
    }

    @Test
    void testFindByIdNotFound() {
        // Given
        given(userRepository.findById(10)).willReturn(Optional.empty());

        // When
        ObjectNotFoundException ex = assertThrows(ObjectNotFoundException.class, () -> userService.findById(10));

        // Then
        assertThat(ex.getMessage()).isEqualTo("Could not find user with Id 10 :(");
        verify(this.userRepository, times(1)).findById(10);
    }

    @Test
    void testSaveSuccess() {
        // Given
        HogwartsUser u = new HogwartsUser();
        u.setUsername("john");
        u.setPassword("123456");
        u.setEnabled(true);
        u.setRoles("admin user");

        given(this.passwordEncoder.encode(u.getPassword())).willReturn("Encoded Password");
        given(this.userRepository.save(u)).willReturn(u);

        // When
        HogwartsUser user = userService.save(u);

        // Then
        assertThat(user.getUsername()).isEqualTo("john");
        assertThat(user.getPassword()).isEqualTo("Encoded Password");
        assertThat(user.isEnabled()).isEqualTo(true);
        assertThat(user.getRoles()).isEqualTo("admin user");
        verify(this.userRepository, times(1)).save(u);
    }

    @Test
    void testUpdateByAdminSuccess() {
        // Given
        HogwartsUser u = new HogwartsUser();
        u.setId(2);
        u.setUsername("eric");
        u.setPassword("654321");
        u.setEnabled(false);
        u.setRoles("user");

        HogwartsUser update = new HogwartsUser();
        update.setUsername("eric - update");
        update.setEnabled(true);
        update.setRoles("admin user");

        given(userRepository.findById(2)).willReturn(Optional.of(u));
        given(userRepository.save(u)).willReturn(u);

        HogwartsUser hogwartsUser = new HogwartsUser();
        hogwartsUser.setRoles("admin");
        MyUserPrincipal myUserPrincipal = new MyUserPrincipal(hogwartsUser);

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(new UsernamePasswordAuthenticationToken(myUserPrincipal, null, myUserPrincipal.getAuthorities()));
        SecurityContextHolder.setContext(securityContext);

        // When
        HogwartsUser updatedUser = userService.update(2, update);

        // Then
        assertThat(updatedUser.getId()).isEqualTo(2);
        assertThat(updatedUser.getUsername()).isEqualTo(update.getUsername());
        assertThat(updatedUser.isEnabled()).isEqualTo(update.isEnabled());
        assertThat(updatedUser.getRoles()).isEqualTo(update.getRoles());
        verify(userRepository, times(1)).findById(2);
        verify(userRepository, times(1)).save(u);
    }

    @Test
    void testUpdateByUserSuccess() {
        // Given
        HogwartsUser u = new HogwartsUser();
        u.setId(2);
        u.setUsername("eric");
        u.setPassword("654321");
        u.setEnabled(false);
        u.setRoles("user");

        HogwartsUser update = new HogwartsUser();
        update.setUsername("eric - update");
        update.setEnabled(false);
        update.setRoles("user");

        given(userRepository.findById(2)).willReturn(Optional.of(u));
        given(userRepository.save(u)).willReturn(u);

        HogwartsUser hogwartsUser = new HogwartsUser();
        hogwartsUser.setRoles("user");
        MyUserPrincipal myUserPrincipal = new MyUserPrincipal(hogwartsUser);

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(new UsernamePasswordAuthenticationToken(myUserPrincipal, null, myUserPrincipal.getAuthorities()));
        SecurityContextHolder.setContext(securityContext);

        // When
        HogwartsUser updatedUser = userService.update(2, update);

        // Then
        assertThat(updatedUser.getId()).isEqualTo(2);
        assertThat(updatedUser.getUsername()).isEqualTo(update.getUsername());
        assertThat(updatedUser.isEnabled()).isEqualTo(update.isEnabled());
        assertThat(updatedUser.getRoles()).isEqualTo(update.getRoles());
        verify(userRepository, times(1)).findById(2);
        verify(userRepository, times(1)).save(u);
    }

    @Test
    void testUpdateNotFound() {
        // Given
        HogwartsUser update = new HogwartsUser();
        update.setUsername("john-update");
        update.setEnabled(true);
        update.setRoles("admin user");

        given(userRepository.findById(10)).willReturn(Optional.empty());

        // When
        ObjectNotFoundException ex = assertThrows(ObjectNotFoundException.class, () -> userService.update(10, update));

        // Then
        assertThat(ex.getMessage()).isEqualTo("Could not find user with Id 10 :(");
         verify(this.userRepository, times(1)).findById(10);
    }

    @Test
    void testDeleteSuccess() {
        // Given
        given(this.userRepository.findById(1)).willReturn(Optional.of(new HogwartsUser()));
        doNothing().when(this.userRepository).deleteById(1);

        // When
        this.userService.delete(1);

        // Then
        verify(this.userRepository, times(1)).deleteById(1);
    }

    @Test
    void testDeleteNotFound() {
        // Given
        given(this.userRepository.findById(1)).willReturn(Optional.empty());

        // When
        ObjectNotFoundException ex = assertThrows(ObjectNotFoundException.class, () -> this.userService.delete(1));

        // Then
        assertThat(ex.getMessage()).isEqualTo("Could not find user with Id 1 :(");
        verify(userRepository, times(1)).findById(1);
    }
}