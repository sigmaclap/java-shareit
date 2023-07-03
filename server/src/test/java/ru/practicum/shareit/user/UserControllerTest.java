package ru.practicum.shareit.user;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserPatchDto;
import ru.practicum.shareit.user.entity.User;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.mapper.UserPatchMapper;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
    private long userId;
    private UserDto userDto;
    private UserPatchDto userToCreate;
    private User userToSave;

    @BeforeEach
    void setUp() {
        userId = 1L;
        userDto = UserDto.builder()
                .id(1L)
                .name("John")
                .email("john@gmail.com")
                .build();
        userToCreate = UserPatchDto.builder()
                .id(1L)
                .name("John")
                .email("john@gmail.com")
                .build();
        userToSave = new User();
    }

    @SneakyThrows
    @Test
    void getUsers_whenValidDataOk_thenReturnedGetUsers() {
        List<User> users = List.of(
                User.builder()
                        .id(1L)
                        .name("John")
                        .email("john@gmail.com")
                        .build()
        );
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

        checkEqualsResult(expectedUsers, result);
    }

    @SneakyThrows
    @Test
    void createUser_whenUserValid_thenReturnedCreateUserStatusCode200() {
        when(userMapper.toUser(userDto)).thenReturn(userToSave);
        when(service.createUser(userToSave)).thenReturn(userToSave);
        when(userMapper.toUserDto(userToSave)).thenReturn(userDto);

        String result = mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        checkEqualsResult(userDto, result);
    }

    @SneakyThrows
    @Test
    void updateUser_whenValidData_thenUpdateUserStatusCode200() {
        when(patchMapper.toUser(userToCreate, userId)).thenReturn(userToSave);
        when(service.updateUser(userToSave, userId)).thenReturn(userToSave);
        when(userMapper.toUserDto(userToSave)).thenReturn(userDto);

        String result = performPatch(userToCreate);

        checkEqualsResult(userToCreate, result);
        verify(service).updateUser(userToSave, userId);
    }

    @SneakyThrows
    @Test
    void updateUser_whenEmptyNameUser_thenUpdateUserStatusCode200() {
        when(patchMapper.toUser(userToCreate, userId)).thenReturn(userToSave);
        when(service.updateUser(userToSave, userId)).thenReturn(userToSave);
        when(userMapper.toUserDto(userToSave)).thenReturn(userDto);

        String result = performPatch(userToCreate);

        checkEqualsResult(userDto, result);
        verify(service).updateUser(userToSave, userId);
    }

    @SneakyThrows
    @Test
    void updateUser_whenEmptyEmailUser_thenUpdateUserStatusCode200() {
        when(patchMapper.toUser(userToCreate, userId)).thenReturn(userToSave);
        when(service.updateUser(userToSave, userId)).thenReturn(userToSave);
        when(userMapper.toUserDto(userToSave)).thenReturn(userDto);

        String result = performPatch(userToCreate);

        checkEqualsResult(userDto, result);
        verify(service).updateUser(userToSave, userId);
    }

    @SneakyThrows
    @Test
    void findUserById_whenValidDataOk_thenReturnedFindUserStatusCode200() {
        mockMvc.perform(get("/users/{id}", userId))
                .andDo(print())
                .andExpect(status().isOk());
        verify(service).findUserById(userId);
    }

    @SneakyThrows
    @Test
    void deleteUserById_whenGetValidUser_thenReturnedSuccessStatusCode200() {
        mockMvc.perform(delete("/users/{id}", userId))
                .andDo(print())
                .andExpect(status().isOk());
        verify(service).deleteUserById(userId);
    }

    private String performPatch(Object expected) throws Exception {
        return mockMvc.perform(patch("/users/{id}", userId)
                        .content(objectMapper.writeValueAsString(expected))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    private void checkEqualsResult(Object expected, String result) throws JsonProcessingException {
        assertEquals(objectMapper.writeValueAsString(expected), result);
    }
}