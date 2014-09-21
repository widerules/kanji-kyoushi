package com.kanjikyoushi.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@XmlRootElement
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "accountId" }))
public class Account {

  @NotNull
  private String accountId;

  @Id
  @GeneratedValue
  private Long id;

  public String getAccountId() {
    return accountId;
  }

  public Long getId() {
    return id;
  }

  public void setAccountId(String accountId) {
    this.accountId = accountId;
  }

  public void setId(Long id) {
    this.id = id;
  }

}
