package com.siae.dto;

import com.siae.entities.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class UserDTO {

    private User user;
    private String role;
}
