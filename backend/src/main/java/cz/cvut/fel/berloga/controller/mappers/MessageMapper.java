package cz.cvut.fel.berloga.controller.mappers;

import cz.cvut.fel.berloga.controller.dto.MessageDTO;
import cz.cvut.fel.berloga.entity.MessageEntity;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(uses = UserMapper.class)
public abstract class MessageMapper {

    @Autowired
    private UserMapper userMapper;

    public MessageDTO toDTO(MessageEntity messageEntity) {
        MessageDTO dto = new MessageDTO();
        dto.setContent(messageEntity.getText());
        if(messageEntity.getFile()!=null) {
            dto.setFileId(messageEntity.getFile().getPseudoName());
            dto.setFileName(messageEntity.getFile().getName());
        }
        dto.setSender(userMapper.toDTO(messageEntity.getSender()));
        dto.setDate(messageEntity.getDate());
        dto.setId(messageEntity.getId());
        return dto;
    }
}
