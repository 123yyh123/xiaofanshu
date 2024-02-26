package com.yyh.xfs.search.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.util.AnnotatedTypeScanner;

import javax.annotation.PostConstruct;

/**
 * @author yyh
 * @date 2024-02-19
 */
@Configuration
public class EsConfig {
    private final ElasticsearchRestTemplate elasticsearchRestTemplate;

    public EsConfig(ElasticsearchRestTemplate elasticsearchRestTemplate) {
        this.elasticsearchRestTemplate = elasticsearchRestTemplate;
    }

    @PostConstruct
    public void createIndex() {
        AnnotatedTypeScanner scanner = new AnnotatedTypeScanner(false, Document.class);
        for (Class clazz : scanner.findTypes("com.yyh.xfs.search.domain")) {
            Document doc = AnnotationUtils.findAnnotation(clazz, Document.class);
            assert doc != null;
            IndexOperations ops = elasticsearchRestTemplate.indexOps(clazz);
            if (!ops.exists()) {
                ops.create();
                ops.refresh();
                ops.putMapping(ops.createMapping());
            }
        }
    }
}
