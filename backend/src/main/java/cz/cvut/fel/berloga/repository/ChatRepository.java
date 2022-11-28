package cz.cvut.fel.berloga.repository;

import cz.cvut.fel.berloga.entity.ChatEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.persistence.NamedNativeQuery;
import java.util.List;

@Repository
@Profile("!test")
public interface ChatRepository extends JpaRepository<ChatEntity, Long> {
    @Query(value = "SELECT c FROM ChatEntity c JOIN ChatUserEntity ce ON c.id = ce.chat.id WHERE ce.user.id = :id")
    List<ChatEntity> findAllByUserId(Long id);

    List<ChatEntity> findAllByNameContains(String name);
}
