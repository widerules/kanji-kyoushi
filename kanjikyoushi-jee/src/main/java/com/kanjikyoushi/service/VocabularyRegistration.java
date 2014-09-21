package com.kanjikyoushi.service;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.apache.log4j.Logger;

import com.kanjikyoushi.model.VocabularyWord;

@Stateless
public class VocabularyRegistration {

  @Inject
  private EntityManager em;

  @Inject
  private transient Logger LOG;

  @Inject
  private Event<VocabularyWord> vocabularyEventSrc;

  public void register(VocabularyWord newWord) {
    /* register new word */
    LOG.info("Registering " + newWord.getWord());
    em.persist(newWord);
    vocabularyEventSrc.fire(newWord);
  }

}
