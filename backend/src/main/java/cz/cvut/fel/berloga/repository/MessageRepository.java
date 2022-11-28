package cz.cvut.fel.berloga.repository;

import cz.cvut.fel.berloga.entity.MessageEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!test")
public interface MessageRepository extends JpaRepository<MessageEntity, Long> {
}
