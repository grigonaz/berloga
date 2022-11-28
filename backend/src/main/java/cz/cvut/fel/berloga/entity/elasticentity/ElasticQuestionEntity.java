package cz.cvut.fel.berloga.entity.elasticentity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.elasticsearch.annotations.Document;

@Document(indexName = "berloga",type = "elastic-question-entity")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ElasticQuestionEntity {
    private String text;
    private Long id;
}