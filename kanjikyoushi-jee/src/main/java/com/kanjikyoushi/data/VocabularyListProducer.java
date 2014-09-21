package com.kanjikyoushi.data;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;
import javax.enterprise.inject.Produces;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.log4j.Logger;

import com.kanjikyoushi.model.VocabularyWord;
import com.kanjikyoushi.service.VocabularyRegistration;

@Named("vocabulary")
@SessionScoped
public class VocabularyListProducer implements Serializable {

  private static final long serialVersionUID = 1147580128140919668L;

  @Inject
  private EntityManager em;

  @Inject
  private FacesContext facesContext;

  @Inject
  private transient Logger LOG;

  private VocabularyWord newWord;

  private VocabularyWord selectedWord;
  @Inject
  private VocabularyRegistration vocabRegistration;

  @Inject
  private Event<VocabularyWord> vocabularyEventSrc;

  private List<VocabularyWord> vocabularyWords;

  public VocabularyWord getNewWord() {
    return newWord;
  }

  private String getRootErrorMessage(Exception e) {
    // Default to general error message that registration failed.
    String errorMessage = "Registration failed. See server log for more information";
    if (e == null) {
      // This shouldn't happen, but return the default messages
      return errorMessage;
    }

    // Start with the exception and recurse to find the root cause
    Throwable t = e;
    while (t != null) {
      // Get the message from the Throwable class instance
      errorMessage = t.getLocalizedMessage();
      t = t.getCause();
    }
    // This is the root cause message
    return errorMessage;
  }

  public VocabularyWord getSelectedWord() {
    return selectedWord;
  }

  @Produces
  public List<VocabularyWord> getVocabularyWords() {
    return vocabularyWords;
  }

  public void initNewWord() {
    newWord = new VocabularyWord();
  }

  public void onVocabularyListChanged(
      @Observes(notifyObserver = Reception.IF_EXISTS) final VocabularyWord vocabularyWord) {
    retrieveAllVocabularyWords();
  }

  @PostConstruct
  private void postConstruct() {
    retrieveAllVocabularyWords();
    initNewWord();
  }

  public void register() {

    try {

      vocabRegistration.register(newWord);
      FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
          "Registered!", "Registration successful");
      facesContext.addMessage(null, m);
      initNewWord();

    } catch (Exception e) {

      LOG.error(e, e);
      String errorMessage = getRootErrorMessage(e);
      FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
          errorMessage, "Registration unsuccessful");
      facesContext.addMessage(null, m);

    }

  }

  private void retrieveAllVocabularyWords() {

    CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
    CriteriaQuery<VocabularyWord> criteriaQuery = criteriaBuilder
        .createQuery(VocabularyWord.class);
    Root<VocabularyWord> wordRoot = criteriaQuery.from(VocabularyWord.class);

    criteriaQuery.select(wordRoot);
    criteriaQuery.orderBy(criteriaBuilder.asc(wordRoot.get("word")));

    vocabularyWords = em.createQuery(criteriaQuery).getResultList();

  }

  public void setSelectedWord(VocabularyWord selectedWord) {
    this.selectedWord = selectedWord;
  }

}
