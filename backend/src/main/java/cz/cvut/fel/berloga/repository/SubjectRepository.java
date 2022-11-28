package cz.cvut.fel.berloga.repository;

import cz.cvut.fel.berloga.entity.SubjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubjectRepository extends JpaRepository<SubjectEntity, Long> {
    @Query(value = "select s from SubjectEntity s join s.users su where su.user.id = ?1")
    List<SubjectEntity> findAllByUserStudying(Long userId);
}
