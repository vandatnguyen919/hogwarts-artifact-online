package edu.tcu.cs.hogwartsartifactsonline.hogwartsuser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.tcu.cs.hogwartsartifactsonline.hogwartsuser.dto.UserDto;
import edu.tcu.cs.hogwartsartifactsonline.system.StatusCode;
import edu.tcu.cs.hogwartsartifactsonline.system.exception.ObjectNotFoundException;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    UserService userService;

    @Autowired
    ObjectMapper objectMapper;

    @Value("${api.endpoint.base-url}")
    String baseUrl;

    List<HogwartsUser> users;

    @BeforeEach
    void setUp() {
        this.users = new ArrayList<>();

        HogwartsUser u1 = new HogwartsUser();
        u1.setId(1);
        u1.setUsername("john");
        u1.setPassword("123456");
        u1.setEnabled(true);
        u1.setRoles("admin user");
        this.users.add(u1);

        HogwartsUser u2 = new HogwartsUser();
        u2.setId(2);
        u2.setUsername("eric");
        u2.setPassword("654321");
        u2.setEnabled(true);
        u2.setRoles("user");
        this.users.add(u2);

        HogwartsUser u3 = new HogwartsUser();
        u3.setId(3);
        u3.setUsername("tom");
        u3.setPassword("qwerty");
        u3.setEnabled(false);
        u3.setRoles("user");
        this.users.add(u3);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testFindAllUsersSuccess() throws Exception {
        // Given
        given(userService.findAll()).willReturn(users);

        // When and then
        this.mockMvc.perform(get(this.baseUrl + "/users").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find All Success"))
                .andExpect(jsonPath("$.data", Matchers.hasSize(this.users.size())))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].username").value("john"))
                .andExpect(jsonPath("$.data[1].id").value(2))
                .andExpect(jsonPath("$.data[1].username").value("eric"));
    }

    @Test
    void testFindUserByIdSuccess() throws Exception {
        // Given
        HogwartsUser u = new HogwartsUser();
        u.setId(1);
        u.setUsername("john");
        u.setPassword("123456");
        u.setEnabled(true);
        u.setRoles("admin user");

        given(userService.findById(1)).willReturn(u);

        // When and then
        this.mockMvc.perform(get(this.baseUrl + "/users/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find One Success"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.username").value("john"));
    }

    @Test
    void testFindUserByIdErrorWithNonExistentId() throws Exception {
        // Given
        given(userService.findById(10)).willThrow(new ObjectNotFoundException("user", 10));

        // When and then
        this.mockMvc.perform(get(this.baseUrl + "/users/10").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find user with Id 10 :("))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testAddUserSuccess() throws Exception {
        // Given
        HogwartsUser u = new HogwartsUser();
        u.setId(10);
        u.setUsername("john");
        u.setPassword("123456");
        u.setEnabled(true);
        u.setRoles("admin user");

        String json = this.objectMapper.writeValueAsString(u);

        given(userService.save(any(HogwartsUser.class))).willReturn(u);

        // When and then
        this.mockMvc.perform(post(this.baseUrl + "/users").accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Add Success"))
                .andExpect(jsonPath("$.data.id").isNotEmpty())
                .andExpect(jsonPath("$.data.username").value("john"))
                .andExpect(jsonPath("$.data.enabled").value(true))
                .andExpect(jsonPath("$.data.roles").value("admin user"));
    }

    @Test
    void testUpdateUserSuccess() throws Exception {
        // Given
        UserDto update = new UserDto(null, "john-update", false, "admin user");

        HogwartsUser updatedUser = new HogwartsUser();
        updatedUser.setId(1);
        updatedUser.setUsername("john-update");
        updatedUser.setEnabled(false);
        updatedUser.setRoles("admin user");

        String json = objectMapper.writeValueAsString(update);

        given(userService.update(eq(1), any(HogwartsUser.class))).willReturn(updatedUser);

        // When and then
        this.mockMvc.perform(put(this.baseUrl + "/users/1").accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Update Success"))
                .andExpect(jsonPath("$.data.id").isNotEmpty())
                .andExpect(jsonPath("$.data.username").value("john-update"))
                .andExpect(jsonPath("$.data.enabled").value(false))
                .andExpect(jsonPath("$.data.roles").value("admin user"));
    }

    @Test
    void testUserUpdateUserErrorWithNonExistentId() throws Exception {
        // Given
        UserDto update = new UserDto(null, "john-update", false, "admin user");

        String json = objectMapper.writeValueAsString(update);

        given(userService.update(eq(10), any(HogwartsUser.class))).willThrow(new ObjectNotFoundException("user", 10));

        // When and then
        this.mockMvc.perform(put(this.baseUrl + "/users/10").accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find user with Id 10 :("))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testUserDeleteSuccess() throws Exception {
        // Given
        doNothing().when(userService).delete(10);

        // When and then
        this.mockMvc.perform(delete(this.baseUrl + "/users/10").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Delete Success"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testUserDeleteErrorWithNonExistentId() throws Exception {
        // Given
        doThrow(new ObjectNotFoundException("user", 10)).when(userService).delete(10);

        // When and then
        this.mockMvc.perform(delete(this.baseUrl + "/users/10").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find user with Id 10 :("))
                .andExpect(jsonPath("$.data").isEmpty());
    }
}