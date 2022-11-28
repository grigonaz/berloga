package cz.cvut.fel.berloga.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
@Builder
public class LoginDTO implements Serializable {
    private String username;
    private String password;

}
