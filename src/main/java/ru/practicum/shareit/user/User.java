package ru.practicum.shareit.user;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
public class User {
    private Long id;
    @NotBlank
    private String name;
    @NotNull
    @Email
    private String email;
}
