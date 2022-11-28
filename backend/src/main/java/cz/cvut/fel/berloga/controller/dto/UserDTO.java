package cz.cvut.fel.berloga.controller.dto;

import cz.cvut.fel.berloga.entity.enums.UserRoleEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO implements Serializable {

    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private Boolean blocked;
    private String password;    // dont set password from BE, only receive from FE
    private List<UserRoleEnum> roles;
    private List<Long> subjects;

}
