package cz.cvut.fel.berloga.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InviteUserDTO implements Serializable {
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private String email;
}
