package cz.cvut.fel.berloga.controller.mappers;

import cz.cvut.fel.berloga.controller.dto.*;
import cz.cvut.fel.berloga.entity.ChatEntity;
import cz.cvut.fel.berloga.entity.ChatUserEntity;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(uses = {MessageMapper.class, UserMapper.class})
public class ChatReturnMapper {

    @Autowired
    private ChatUserEntityMapper chatUserEntityMapper;

    @Autowired
    private MessageMapper messageMapper;

    public ChatReturnDTO toDTO(ChatEntity chatEntity) {
        ChatReturnDTO dto = new ChatReturnDTO();
        dto.setChatId(chatEntity.getId());
        dto.setChatName(chatEntity.getName());
        dto.setChatId(chatEntity.getId());
        List<MessageDTO> messageDTOList = chatEntity.getMessages().stream()
                .map(messageMapper::toDTO)
                .collect(Collectors.toList());
        dto.setMessages(messageDTOList);
        List<ChatUserEntityDTO> userDTOList = chatEntity.getParticipators().stream()
                .map(chatUserEntityMapper::toDTO)
                .collect(Collectors.toList());
        dto.setUsers(userDTOList);
        return dto;
    }
}
