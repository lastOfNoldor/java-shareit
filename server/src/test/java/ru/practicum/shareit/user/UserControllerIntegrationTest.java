// CHECKSTYLE:OFF
package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.model.User;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    private User savedUser;

    @BeforeEach
    void setUp() {
        savedUser = userRepository.save(createUser("Test User", "test@email.com"));
    }

    @Test
    void findAllUsers_ShouldReturnAllUsers() throws Exception {
        User user1 = userRepository.save(createUser("User1", "user1@email.com"));
        User user2 = userRepository.save(createUser("User2", "user2@email.com"));

        mockMvc.perform(get("/users")).andExpect(status().isOk()).andExpect(jsonPath("$").isArray()).andExpect(jsonPath("$.length()").value(3)).andExpect(jsonPath("$[0].name").value("Test User")).andExpect(jsonPath("$[0].email").value("test@email.com")).andExpect(jsonPath("$[1].name").value("User1")).andExpect(jsonPath("$[1].email").value("user1@email.com")).andExpect(jsonPath("$[2].name").value("User2")).andExpect(jsonPath("$[2].email").value("user2@email.com"));
    }

    @Test
    void createUser_WithValidData_ShouldReturnUser() throws Exception {
        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(createUserJson("New User", "new@email.com"))).andExpect(status().isOk()).andExpect(jsonPath("$.id").exists()).andExpect(jsonPath("$.name").value("New User")).andExpect(jsonPath("$.email").value("new@email.com"));
    }

    @Test
    void createUser_WithDuplicateEmail_ShouldReturnConflict() throws Exception {
        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(createUserJson("Duplicate User", "test@email.com"))) // Дублируем email существующего пользователя
                .andExpect(status().isConflict());
    }

    @Test
    void findUserById_WhenUserExists_ShouldReturnUser() throws Exception {
        mockMvc.perform(get("/users/{id}", savedUser.getId())).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(savedUser.getId())).andExpect(jsonPath("$.name").value("Test User")).andExpect(jsonPath("$.email").value("test@email.com"));
    }

    @Test
    void findUserById_WhenUserNotExists_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/users/999")).andExpect(status().isNotFound());
    }

    @Test
    void updateUser_ShouldUpdateAndReturnUser() throws Exception {
        mockMvc.perform(patch("/users/{id}", savedUser.getId()).contentType(MediaType.APPLICATION_JSON).content(createUserJson("Updated Name", "updated@email.com"))).andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Updated Name")).andExpect(jsonPath("$.email").value("updated@email.com"));
    }

    @Test
    void deleteUser_ShouldDeleteUser() throws Exception {
        mockMvc.perform(delete("/users/{id}", savedUser.getId())).andExpect(status().isOk());

        mockMvc.perform(get("/users/{id}", savedUser.getId())).andExpect(status().isNotFound());
    }

    private User createUser(String name, String email) {
        return new User(null, name, email);
    }

    private String createUserJson(String name, String email) {
        return String.format("""
                {
                    "name": "%s",
                    "email": "%s"
                }
                """, name, email);
    }
}
// CHECKSTYLE:ON