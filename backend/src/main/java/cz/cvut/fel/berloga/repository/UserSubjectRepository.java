package cz.cvut.fel.berloga.repository;

import cz.cvut.fel.berloga.entity.UserSubjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSubjectRepository extends JpaRepository<UserSubjectEntity, Long> {
}
