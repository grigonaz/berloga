package cz.cvut.fel.berloga.service;

import cz.cvut.fel.berloga.controller.dto.CommentDTO;
import cz.cvut.fel.berloga.controller.dto.QuestionForumDTO;
import cz.cvut.fel.berloga.entity.CommentEntity;
import cz.cvut.fel.berloga.entity.QuestionForumEntity;
import cz.cvut.fel.berloga.entity.UserEntity;
import cz.cvut.fel.berloga.entity.UserRoleEntity;
import cz.cvut.fel.berloga.entity.elasticentity.ElasticQuestionEntity;
import cz.cvut.fel.berloga.entity.enums.UserRoleEnum;
import cz.cvut.fel.berloga.repository.CommentRepository;
import cz.cvut.fel.berloga.repository.QuestionForumRepository;
import cz.cvut.fel.berloga.repository.SubjectRepository;
import cz.cvut.fel.berloga.repository.elastic.ESForumRepository;
import cz.cvut.fel.berloga.service.exceptions.*;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.net.ConnectException;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.elasticsearch.index.query.QueryBuilders.*;

@Service
public class ForumService {

    private final QuestionForumRepository questionRepository;
    private final SubjectRepository subjectRepository;
    private final SessionService sessionService;
    private final CommentRepository commentRepository;
    private final ESForumRepository esForumRepository;
    private final ElasticsearchOperations elasticsearchTemplate;

    public ForumService(QuestionForumRepository questionRepository, SubjectRepository subjectRepository, SessionService sessionService, CommentRepository commentRepository, ESForumRepository esForumRepository, ElasticsearchOperations elasticsearchTemplate) {
        this.questionRepository = questionRepository;
        this.subjectRepository = subjectRepository;
        this.sessionService = sessionService;
        this.commentRepository = commentRepository;
        this.esForumRepository = esForumRepository;
        this.elasticsearchTemplate = elasticsearchTemplate;
    }

    @Transactional
    public QuestionForumEntity getQuestion(Long id) {

        QuestionForumEntity question = questionRepository.findById(id).orElse(null);
        if (question == null) {
            throw new DoesNotExistException("Question with id " + id + " does not exist");
        }
        if (question.getDeleted()) {
            throw new DoesNotExistException("Question with id " + id + " is deleted");
        }
        return question;
    }

    @Transactional
    public List<QuestionForumEntity> getForum() {
        return questionRepository.findQuestionForumEntitiesByDeletedIsFalse()
                .stream()
                .sorted(Comparator.comparing(QuestionForumEntity::getDateTime))
                .collect(Collectors.toList());
    }

    @Transactional
    public List<QuestionForumEntity> getForumWithSearch(String part) {
        Query searchQuery = new NativeSearchQueryBuilder()
                .withQuery(matchQuery("text", part).fuzziness(1))
                .withSort(SortBuilders.scoreSort())
                .build();

        SearchHits<ElasticQuestionEntity> elQuestions =
                elasticsearchTemplate.search(searchQuery, ElasticQuestionEntity.class, IndexCoordinates.of("berloga"));
        List<ElasticQuestionEntity> elasticList = elQuestions.stream().map(SearchHit::getContent).collect(Collectors.toList());

        List<QuestionForumEntity> straight = questionRepository.findQuestionForumEntitiesByDeletedIsFalse()
                .stream()
                .sorted(Comparator.comparing(QuestionForumEntity::getDateTime))
                .collect(Collectors.toList());

        List<QuestionForumEntity> questionsToReturn = new ArrayList<>();

        for (ElasticQuestionEntity elEntity : elasticList) {
            for (QuestionForumEntity q : straight) {
                if (elEntity.getId().equals(q.getId())) {
                    questionsToReturn.add(q);
                }
            }
        }
        return questionsToReturn;
    }

    @Transactional
    public QuestionForumEntity createQuestion(QuestionForumDTO questionDTO) {
        try{
            if (!sessionService.isLogged()) {
                throw new LoginException("Must be logged in to remove the question");
            }
            Objects.requireNonNull(questionDTO);
            Objects.requireNonNull(questionDTO.getQuestion());
            if (questionDTO.getQuestion().length() == 0) {
                throw new WrongInputException("Question should have a content");
            }
            UserEntity userEntity = sessionService.getSession().getUser();
            QuestionForumEntity question = new QuestionForumEntity();
            question.setQuestion(questionDTO.getQuestion());
            question.setDeleted(false);
            question.setDone(false);
            question.setQuestioner(userEntity);
            if (questionDTO.getSubjectId() != null) {
                question.setSubject(subjectRepository.findById(questionDTO.getSubjectId()).orElse(null));
            }
            question.setDateTime(OffsetDateTime.now());
            questionRepository.save(question);
            ElasticQuestionEntity elQuestion = new ElasticQuestionEntity();
            elQuestion.setId(question.getId());
            elQuestion.setText(question.getQuestion());
            esForumRepository.save(elQuestion);
            return question;
        } catch(LoginException e){
            throw new LoginException("Must be logged in to remove the question");
        } catch(WrongInputException e){
            throw new WrongInputException("Question should have a content");
        } catch (Exception e){
            throw new BerlogaException("Sorry, can't save the question because we forgot to turn on the database");
        }

    }

    @Transactional
    public void removeQuestion(Long questionId) {
        Objects.requireNonNull(questionId);
        QuestionForumEntity question = questionRepository.findById(questionId).orElse(null);
        if (question == null) {
            throw new DoesNotExistException("Question with id " + questionId + " doesn't exist");
        }
        if (!sessionService.isLogged()) {
            throw new LoginException("Must be logged in to remove the question");
        }
        if (!sessionService.getSession().getUser().getId().equals(question.getQuestioner().getId())) {
            throw new LoginException("Must be a questioner to remove the question");
        }
        question.setDeleted(true);
        questionRepository.save(question);
    }

    @Transactional
    public void markQuestionDone(Long questionId) {
        Objects.requireNonNull(questionId);
        QuestionForumEntity question = questionRepository.findById(questionId).orElse(null);
        if (question == null) {
            throw new DoesNotExistException("Question with id " + questionId + " doesn't exist");
        }
        UserEntity logged = sessionService.getSession().getUser();
        List<UserRoleEnum> loggedRoles = logged.getRoles().stream().map(UserRoleEntity::getUserRole).collect(Collectors.toList());
        if (!question.getQuestioner().getId().equals(logged.getId()) && !loggedRoles.contains(UserRoleEnum.MODERATOR)) {
            throw new PermissionException("Only moderator or questioner can mark question as Done");
        }
        question.setDone(true);
        questionRepository.save(question);
    }


    public void editQuestion(QuestionForumDTO questionDTO) {
        try{
            Objects.requireNonNull(questionDTO);
            Objects.requireNonNull(questionDTO.getQuestion());
            Objects.requireNonNull(questionDTO.getId());

            QuestionForumEntity question = questionRepository.findById(questionDTO.getId()).orElse(null);
            if (question == null) {
                throw new DoesNotExistException("Question with id " + questionDTO.getId() + " doesn't exist");
            }
            UserEntity logged = sessionService.getSession().getUser();
            List<UserRoleEnum> loggedRoles = logged.getRoles().stream().map(UserRoleEntity::getUserRole).collect(Collectors.toList());

            if (!question.getQuestioner().getId().equals(logged.getId()) && !loggedRoles.contains(UserRoleEnum.MODERATOR)) {
                throw new PermissionException("Only moderator or questioner can mark question as Done");
            }
            question.setQuestion(questionDTO.getQuestion());
            if (questionDTO.getSubjectId() != null) {
                question.setSubject(subjectRepository.findById(questionDTO.getSubjectId()).orElse(null));
            }
            questionRepository.save(question);
            ElasticQuestionEntity elQuestion =  esForumRepository.findById(question.getId());
            elQuestion.setText(question.getQuestion());
            esForumRepository.save(elQuestion);
        } catch(DoesNotExistException e){
            throw new DoesNotExistException("Question with id " + questionDTO.getId() + " doesn't exist");
        } catch(PermissionException e){
            throw new PermissionException("Only moderator or questioner can mark question as Done");
        } catch (Exception e){
            throw new BerlogaException("Sorry, can't save the question because we forgot to turn on the database");
        }

    }

    @Transactional
    public void commentQuestion(CommentDTO commentDTO) {
        Objects.requireNonNull(commentDTO.getQuestionId());
        Objects.requireNonNull(commentDTO);
        Objects.requireNonNull(commentDTO.getContent());
        QuestionForumEntity question = questionRepository.findById(commentDTO.getQuestionId()).orElse(null);
        if (question == null) {
            throw new DoesNotExistException("Question with id " + commentDTO.getQuestionId() + " doesn't exist");
        }
        UserEntity logged = sessionService.getSession().getUser();
        CommentEntity comment = new CommentEntity();
        comment.setQuestion(question);
        comment.setContent(commentDTO.getContent());
        comment.setSender(logged);
        comment.setDateTime(OffsetDateTime.now());
        commentRepository.save(comment);
    }

    @Transactional
    public void removeComment(Long commentId) {
        Objects.requireNonNull(commentId);
        CommentEntity commentEntity = commentRepository.findById(commentId).orElse(null);
        if (commentEntity == null) {
            throw new DoesNotExistException("Comment with id " + commentId + " doesn't exist");
        }
        commentRepository.delete(commentEntity);
    }

}
