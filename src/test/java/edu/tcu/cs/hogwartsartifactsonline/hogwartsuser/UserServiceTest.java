package edu.tcu.cs.hogwartsartifactsonline.hogwartsuser;

import edu.tcu.cs.hogwartsartifactsonline.system.exception.ObjectNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;

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

        given(userRepository.save(u)).willReturn(u);

        // When
        HogwartsUser user = userService.save(u);

        // Then
        assertThat(user.getUsername()).isEqualTo("john");
        assertThat(user.getPassword()).isEqualTo("123456");
        assertThat(user.isEnabled()).isEqualTo(true);
        assertThat(user.getRoles()).isEqualTo("admin user");
        verify(this.userRepository, times(1)).save(u);
    }

    @Test
    void testUpdateSuccess() {
        // Given
        HogwartsUser u = new HogwartsUser();
        u.setId(1);
        u.setUsername("john");
        u.setPassword("123456");
        u.setEnabled(true);
        u.setRoles("admin user");

        HogwartsUser update = new HogwartsUser();
        update.setUsername("john-update");
        update.setEnabled(true);
        update.setRoles("admin user");

        given(userRepository.findById(1)).willReturn(Optional.of(u));
        given(userRepository.save(u)).willReturn(u);

        // When
        HogwartsUser updatedUser = userService.update(1, update);

        // Then
        assertThat(updatedUser.getId()).isEqualTo(1);
        assertThat(updatedUser.getUsername()).isEqualTo("john-update");
        assertThat(updatedUser.isEnabled()).isEqualTo(true);
        assertThat(updatedUser.getRoles()).isEqualTo("admin user");
        verify(userRepository, times(1)).findById(1);
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