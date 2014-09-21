package com.kanjikyoushi.rest;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaQuery;

import org.apache.log4j.Logger;

import com.kanjikyoushi.model.VocabularyWord;

public class VocabularyRepositoryTestImpl implements VocabularyRepository {

  @Inject
  private EntityManager em;

  @Inject
  private Logger LOG;

  @Override
  public List<VocabularyWord> getAll() {

    LOG.info("get all");

    CriteriaQuery<VocabularyWord> criteriaQuery = em.getCriteriaBuilder().createQuery(
        VocabularyWord.class);
    criteriaQuery.select(criteriaQuery.from(VocabularyWord.class));

    return em.createQuery(criteriaQuery).getResultList();

  }

}
