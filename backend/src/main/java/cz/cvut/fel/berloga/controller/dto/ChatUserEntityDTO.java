package cz.cvut.fel.berloga.controller.dto;

import cz.cvut.fel.berloga.entity.enums.UserChatRoleEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatUserEntityDTO  implements Serializable {
    private UserChatRoleEnum role;
    private UserDTO user;
}
