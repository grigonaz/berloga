package cz.cvut.fel.berloga.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
@Builder
public class EditProfileDTO  implements Serializable {
    private String email;
    private String password;
}
