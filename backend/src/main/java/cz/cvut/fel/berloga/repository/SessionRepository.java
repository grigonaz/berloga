package cz.cvut.fel.berloga.repository;

import cz.cvut.fel.berloga.entity.SessionEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Profile("!test")
public interface SessionRepository extends JpaRepository<SessionEntity, Long> {
    List<SessionEntity> findAllBySession(String session);
    List<SessionEntity> findAllByUserId(Long userId);
}
