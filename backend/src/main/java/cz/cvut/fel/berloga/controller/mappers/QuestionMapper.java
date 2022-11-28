package cz.cvut.fel.berloga.controller.mappers;

import cz.cvut.fel.berloga.controller.dto.CommentDTO;
import cz.cvut.fel.berloga.controller.dto.QuestionForumDTO;
import cz.cvut.fel.berloga.entity.CommentEntity;
import cz.cvut.fel.berloga.entity.QuestionForumEntity;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(uses = UserMapper.class)
public abstract class QuestionMapper {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CommentMapper commentMapper;

    public QuestionForumDTO toDTO(QuestionForumEntity entity) {
        QuestionForumDTO dto = new QuestionForumDTO();
        dto.setId(entity.getId());
        dto.setQuestion(entity.getQuestion());
        dto.setSender(userMapper.toDTO(entity.getQuestioner()));
        dto.setDateTime(entity.getDateTime());
        List<CommentDTO> comments = entity.getComments().stream()
                .map(commentMapper::toDTO)
                .collect(Collectors.toList());
        dto.setComments(comments);
        dto.setDone(entity.getDone());
        return dto;
    }
}
