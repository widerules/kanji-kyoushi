package com.kanjikyoushi.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@XmlRootElement
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "word" }))
public class VocabularyWord implements Serializable {

  private static final long serialVersionUID = 2446857210963221919L;

  @Id
  @GeneratedValue
  private Long id;

  @ElementCollection(fetch = FetchType.EAGER)
  private Set<String> meanings;

  @ElementCollection(fetch = FetchType.EAGER)
  private Set<String> readings;

  @NotNull
  private String word;

  public Long getId() {
    return id;
  }

  public List<String> getMeanings() {
    return new ArrayList<String>(meanings);
  }

  public List<String> getReadings() {
    return new ArrayList<String>(readings);
  }

  public String getWord() {
    return word;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setMeanings(List<String> meanings) {
    this.readings = new HashSet<String>(meanings);
  }

  public void setReadings(List<String> readings) {
    this.readings = new HashSet<String>(readings);
  }

  public void setWord(String word) {
    this.word = word;
  }

}
