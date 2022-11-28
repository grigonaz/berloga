package cz.cvut.fel.berloga.repository;

import cz.cvut.fel.berloga.entity.QuestionForumEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionForumRepository extends JpaRepository<QuestionForumEntity, Long> {
    List<QuestionForumEntity> findQuestionForumEntitiesByDeletedIsFalse();
}
