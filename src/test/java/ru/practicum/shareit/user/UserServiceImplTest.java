package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exceptions.UserNotFoundException;
import ru.practicum.shareit.user.entity.User;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository repository;

    @InjectMocks
    private UserServiceImpl userServiceImpl;

    @Test
    void getUsers_whenCorrectData_thenReturnedListUsers() {
        List<User> expectedUsers = List.of(new User());
        when(repository.findAll()).thenReturn(expectedUsers);

        List<User> actualUsers = userServiceImpl.getUsers();

        assertEquals(1, actualUsers.size());
        assertEquals(expectedUsers, actualUsers);
    }

    @Test
    void createUser_whenUserValid_thenSavedUser() {
        User expectedUser = new User();
        when(repository.save(expectedUser)).thenReturn(expectedUser);

        User actualUser = userServiceImpl.createUser(expectedUser);

        assertEquals(expectedUser, actualUser);
        verify(repository).save(expectedUser);
    }

    @Test
    void updateUser_whenUserFound_thenReturnedUser() {
        long userId = 0L;
        User expectedUser = new User();
        when(repository.save(expectedUser)).thenReturn(expectedUser);

        User actualUser = userServiceImpl.updateUser(expectedUser, userId);

        assertEquals(expectedUser, actualUser);
        verify(repository, times(1)).save(expectedUser);
    }

    @Test
    void findUserById_whenUserFound_thenReturnedUser() {
        long userId = 0L;
        User expectedUser = new User();
        when(repository.findById(userId)).thenReturn(Optional.of(expectedUser));

        User actualUser = userServiceImpl.findUserById(userId);

        assertEquals(expectedUser, actualUser);
    }

    @Test
    void findUserById_whenUserNotFound_thenReturnedUser() {
        long userId = 0L;
        when(repository.findById(userId)).thenReturn(Optional.empty());

        Throwable exception = assertThrows(UserNotFoundException.class,
                () -> userServiceImpl.findUserById(userId));

        assertEquals("User with id not found", exception.getMessage());
    }

    @Test
    void deleteUserById_whenUserFound_thenDeletedUser() {
        long userId = 0L;
        User expectedUser = new User();
        when(repository.findById(userId)).thenReturn(Optional.of(expectedUser));

        userServiceImpl.deleteUserById(userId);

        verify(repository, times(1)).delete(expectedUser);
    }
}