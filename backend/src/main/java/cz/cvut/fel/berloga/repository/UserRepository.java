package cz.cvut.fel.berloga.repository;

import cz.cvut.fel.berloga.entity.UserEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!test")
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    UserEntity getByUsernameEquals(String username);
    UserEntity getByEmailEquals(String username);
}
