package cz.cvut.fel.berloga.repository.elastic;

import cz.cvut.fel.berloga.entity.elasticentity.ElasticQuestionEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface ESForumRepository extends ElasticsearchRepository<ElasticQuestionEntity, String> {
    @Query("{\"wildcard\":{\"text\":{\"query\": \"?0\"}}}")
    List<ElasticQuestionEntity> findAllSort(String string);

    ElasticQuestionEntity findById(Long id);
}
