package cz.cvut.fel.berloga.controller.mappers;

import cz.cvut.fel.berloga.controller.dto.ChatUserEntityDTO;
import cz.cvut.fel.berloga.entity.ChatUserEntity;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Mapper(uses = UserMapper.class)
public abstract class ChatUserEntityMapper {

    @Autowired
    private UserMapper userMapper;

    public ChatUserEntityDTO toDTO(ChatUserEntity chatUserEntity){
        ChatUserEntityDTO dto = new ChatUserEntityDTO();
        dto.setUser(userMapper.toDTO(chatUserEntity.getUser()));
        dto.setRole(chatUserEntity.getRole());
        return dto;
    }
}
