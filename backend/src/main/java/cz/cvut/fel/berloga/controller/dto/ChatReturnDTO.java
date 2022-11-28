package cz.cvut.fel.berloga.controller.dto;

import cz.cvut.fel.berloga.entity.ChatUserEntity;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatReturnDTO implements Serializable {
    private Long chatId;
    private String chatName;
    private List<ChatUserEntityDTO> users;
    private List<MessageDTO> messages;
}
