package cz.cvut.fel.berloga.controller.mappers;

import cz.cvut.fel.berloga.controller.dto.ChatDTO;
import cz.cvut.fel.berloga.controller.dto.ChatReturnDTO;
import cz.cvut.fel.berloga.controller.dto.ChatUserEntityDTO;
import cz.cvut.fel.berloga.entity.ChatEntity;
import cz.cvut.fel.berloga.entity.ChatUserEntity;
import org.mapstruct.Mapper;

@Mapper
public abstract class ChatMapper {

    public ChatDTO toDTO(ChatEntity chatEntity){
        ChatDTO dto = new ChatDTO();
        dto.setChatId(chatEntity.getId());
        dto.setChatName(chatEntity.getName());
        return dto;
    }
}
