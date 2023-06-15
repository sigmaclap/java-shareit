package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.WebApplicationContext;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserPatchDto;
import ru.practicum.shareit.user.entity.User;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.mapper.UserPatchMapper;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@SpringJUnitWebConfig(UserController.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private UserService service;
    @MockBean
    private UserMapper userMapper;
    @MockBean
    private UserPatchMapper patchMapper;

    @BeforeEach
    void setUp(WebApplicationContext wac) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .build();
    }

    @SneakyThrows
    @Test
    void getUsers() {
        List<User> users = List.of(
                User.builder()
                        .id(1L)
                        .name("John")
                        .email("john@gmail.com")
                        .build()
        );
        UserDto userDto = UserDto.builder()
                .id(1L)
                .name("John")
                .email("john@gmail.com")
                .build();
        when(service.getUsers()).thenReturn(users);
        when(userMapper.toUserDto(users.get(0))).thenReturn(userDto);
        List<UserDto> expectedUsers = users.stream()
                .map(userMapper::toUserDto)
                .collect(Collectors.toList());

        String result = mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(expectedUsers), result);
    }

    @SneakyThrows
    @Test
    void createUser_whenUserValid_thenReturnedCreateUser() {
        UserDto userToCreate = UserDto.builder()
                .id(1L)
                .name("John")
                .email("john@gmail.com")
                .build();
        User userToSave = new User();
        when(userMapper.toUser(userToCreate)).thenReturn(userToSave);
        when(service.createUser(userToSave)).thenReturn(userToSave);
        when(userMapper.toUserDto(userToSave)).thenReturn(userToCreate);

        String result = mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(userToCreate))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();


        assertEquals(objectMapper.writeValueAsString(userToCreate), result);
    }

    @SneakyThrows
    @Test
    void createUser_whenUserIsNotValid_thenReturnedBadRequest() {
        User userToCreate = new User();
        when(service.createUser(userToCreate)).thenReturn(userToCreate);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userToCreate)))
                .andDo(print())
                .andExpect(status().is(400));
        verify(service, never()).createUser(userToCreate);
    }

    @SneakyThrows
    @Test
    void updateUser_whenValidData_thenUpdateUser() {
        long userId = 1L;
        UserPatchDto userToCreate = UserPatchDto.builder()
                .id(1L)
                .name("John")
                .email("john@gmail.com")
                .build();
        User userToSave = new User();
        UserDto userDto = UserDto.builder()
                .id(1L)
                .name("John")
                .email("john@gmail.com")
                .build();
        when(patchMapper.toUser(userToCreate, userId)).thenReturn(userToSave);
        when(service.updateUser(userToSave, userId)).thenReturn(userToSave);
        when(userMapper.toUserDto(userToSave)).thenReturn(userDto);

        String result = mockMvc.perform(patch("/users/{id}", userId)
                        .content(objectMapper.writeValueAsString(userToCreate))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(userToCreate), result);
        verify(service).updateUser(userToSave, userId);
    }

    @SneakyThrows
    @Test
    void updateUser_whenEmptyNameUser_thenUpdateUser() {
        long userId = 1L;
        UserPatchDto userToCreate = UserPatchDto.builder()
                .id(1L)
                .email("ivan@gmail.com")
                .build();
        User userToSave = new User();
        UserDto userDto = UserDto.builder()
                .id(1L)
                .name("John")
                .email("john@gmail.com")
                .build();
        when(patchMapper.toUser(userToCreate, userId)).thenReturn(userToSave);
        when(service.updateUser(userToSave, userId)).thenReturn(userToSave);
        when(userMapper.toUserDto(userToSave)).thenReturn(userDto);

        String result = mockMvc.perform(patch("/users/{id}", userId)
                        .content(objectMapper.writeValueAsString(userToCreate))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(userDto), result);
        verify(service).updateUser(userToSave, userId);
    }

    @SneakyThrows
    @Test
    void updateUser_whenEmptyEmailUser_thenUpdateUser() {
        long userId = 1L;
        UserPatchDto userToCreate = UserPatchDto.builder()
                .id(1L)
                .name("name")
                .build();
        User userToSave = new User();
        UserDto userDto = UserDto.builder()
                .id(1L)
                .name("John")
                .email("john@gmail.com")
                .build();
        when(patchMapper.toUser(userToCreate, userId)).thenReturn(userToSave);
        when(service.updateUser(userToSave, userId)).thenReturn(userToSave);
        when(userMapper.toUserDto(userToSave)).thenReturn(userDto);

        String result = mockMvc.perform(patch("/users/{id}", userId)
                        .content(objectMapper.writeValueAsString(userToCreate))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(userDto), result);
        verify(service).updateUser(userToSave, userId);
    }

    @SneakyThrows
    @Test
    void updateUser_whenNotValidEmailUser_thenUpdateUser() {
        long userId = 1L;
        UserPatchDto userToCreate = UserPatchDto.builder()
                .id(1L)
                .name("name")
                .email("email")
                .build();
        User userToSave = new User();
        UserDto userDto = UserDto.builder()
                .id(1L)
                .name("John")
                .email("john@gmail.com")
                .build();
        when(patchMapper.toUser(userToCreate, userId)).thenReturn(userToSave);
        when(service.updateUser(userToSave, userId)).thenReturn(userToSave);
        when(userMapper.toUserDto(userToSave)).thenReturn(userDto);

        Object ex = Objects.requireNonNull(mockMvc.perform(patch("/users/{id}", userId)
                                .content(objectMapper.writeValueAsString(userToCreate))
                                .characterEncoding(StandardCharsets.UTF_8)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                        .andDo(print())
                        .andExpect(status().is(400))
                        .andReturn()
                        .getResolvedException())
                .getClass();

        assertEquals(MethodArgumentNotValidException.class, ex);
        verify(service, never()).updateUser(userToSave, userId);
    }

    @SneakyThrows
    @Test
    void findUserById() {
        long userId = 0L;

        mockMvc.perform(get("/users/{id}", userId))
                .andDo(print())
                .andExpect(status().isOk());
        verify(service).findUserById(userId);
    }

    @SneakyThrows
    @Test
    void deleteUserById() {
        long userId = 0L;

        mockMvc.perform(delete("/users/{id}", userId))
                .andDo(print())
                .andExpect(status().isOk());
        verify(service).deleteUserById(userId);
    }
}