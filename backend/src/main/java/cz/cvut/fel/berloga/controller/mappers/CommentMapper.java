package cz.cvut.fel.berloga.controller.mappers;

import cz.cvut.fel.berloga.controller.dto.CommentDTO;
import cz.cvut.fel.berloga.controller.dto.MessageDTO;
import cz.cvut.fel.berloga.entity.CommentEntity;
import cz.cvut.fel.berloga.entity.MessageEntity;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(uses = UserMapper.class)
public abstract class CommentMapper {

    @Autowired
    private UserMapper userMapper;

    public CommentDTO toDTO(CommentEntity commentEntity) {
        CommentDTO dto = new CommentDTO();
        dto.setId(commentEntity.getId());
        dto.setContent(commentEntity.getContent());
        dto.setSender(userMapper.toDTO(commentEntity.getSender()));
        dto.setDateTime(commentEntity.getDateTime());
        return dto;
    }

}
