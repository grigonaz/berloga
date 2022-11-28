package cz.cvut.fel.berloga.controller;

import cz.cvut.fel.berloga.controller.dto.CommentDTO;
import cz.cvut.fel.berloga.controller.dto.QuestionForumDTO;
import cz.cvut.fel.berloga.controller.dto.StatusDTO;
import cz.cvut.fel.berloga.controller.mappers.QuestionMapper;
import cz.cvut.fel.berloga.entity.QuestionForumEntity;
import cz.cvut.fel.berloga.service.ForumService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/forum")
public class ForumController {

    private final ForumService forumService;
    private final QuestionMapper questionMapper;

    @Autowired
    public ForumController(ForumService forumService, QuestionMapper questionMapper) {
        this.forumService = forumService;
        this.questionMapper = questionMapper;
    }

    @GetMapping(path = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getForum() {
        List<QuestionForumEntity> questionList = forumService.getForum();
        List<QuestionForumDTO> dtoList = questionList.stream().map(questionMapper::toDTO).collect(Collectors.toList());
        return ResponseEntity.status(200).body(dtoList);
    }

    @PostMapping(path = "/all-search", produces = MediaType.APPLICATION_JSON_VALUE,consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getForumWithSearch(@RequestBody String part) {
        List<QuestionForumEntity> questionList = forumService.getForumWithSearch(part);
        List<QuestionForumDTO> dtoList = questionList.stream().map(questionMapper::toDTO).collect(Collectors.toList());
        return ResponseEntity.status(200).body(dtoList);
    }

    @GetMapping(path = "/question/{question_id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getQuestion(@PathVariable Long question_id) {
        QuestionForumEntity question = forumService.getQuestion(question_id);
        return ResponseEntity.status(200).body(questionMapper.toDTO(question));
    }

    @PostMapping(path = "/create-question", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StatusDTO> createQuestion(@RequestBody QuestionForumDTO questionForumDTO) {
        forumService.createQuestion(questionForumDTO);
        return ResponseEntity.ok(StatusDTO.builder().status("Question created").build());
    }

    @DeleteMapping(path = "/questions/{question_id}")
    public ResponseEntity<StatusDTO> removeQuestion(@PathVariable Long question_id) {
        forumService.removeQuestion(question_id);
        return ResponseEntity.ok(StatusDTO.builder().status("Question" + question_id + " deleted").build());
    }

    @PostMapping(path = "/edit-question", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StatusDTO> editQuestion(@RequestBody QuestionForumDTO questionForumDTO) {
        forumService.editQuestion(questionForumDTO);
        return ResponseEntity.ok(StatusDTO.builder().status("Question edited").build());
    }

    @PostMapping(path = "/mark-done/{question_id}")
    public ResponseEntity<StatusDTO> markDone(@PathVariable Long question_id) {
        forumService.markQuestionDone(question_id);
        return ResponseEntity.ok(StatusDTO.builder().status("Marked as done").build());
    }

    @PostMapping(path = "/comment-question", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StatusDTO> createComment(@RequestBody CommentDTO commentDTO) {
        forumService.commentQuestion(commentDTO);
        return ResponseEntity.ok(StatusDTO.builder().status("Comment created").build());
    }

    @DeleteMapping(path = "/remove-comment/{comment_id}")
    public ResponseEntity<StatusDTO> removeComment(@PathVariable Long comment_id) {
        forumService.removeComment(comment_id);
        return ResponseEntity.ok(StatusDTO.builder().status("Comment deleted").build());
    }


}
