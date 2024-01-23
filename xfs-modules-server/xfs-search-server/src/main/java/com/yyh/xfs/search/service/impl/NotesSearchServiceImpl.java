package com.yyh.xfs.search.service.impl;

import com.yyh.xfs.notes.domain.NotesDO;
import com.yyh.xfs.search.domain.NotesEsDO;
import com.yyh.xfs.search.service.NotesSearchService;
import org.springframework.beans.BeanUtils;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.stereotype.Service;

/**
 * @author yyh
 * @date 2024-01-24
 */
@Service
public class NotesSearchServiceImpl implements NotesSearchService {
    private final ElasticsearchRestTemplate elasticsearchRestTemplate;

    public NotesSearchServiceImpl(ElasticsearchRestTemplate elasticsearchRestTemplate) {
        this.elasticsearchRestTemplate = elasticsearchRestTemplate;
    }

    @Override
    public void addNotes(NotesDO notesDO) {
        NotesEsDO notesEsDO = new NotesEsDO();
        BeanUtils.copyProperties(notesDO, notesEsDO);
        GeoPoint geoPoint = new GeoPoint(notesDO.getLatitude(), notesDO.getLongitude());
        notesEsDO.setLocation(geoPoint);
        elasticsearchRestTemplate.save(notesEsDO);
    }
}
