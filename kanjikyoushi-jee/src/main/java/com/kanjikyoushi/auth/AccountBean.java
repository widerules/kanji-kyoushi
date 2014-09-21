package com.kanjikyoushi.auth;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

@Named("account")
@SessionScoped
public class AccountBean implements Serializable {

  private static final long serialVersionUID = 2709358568760137049L;

  private String loginId;
  @Inject
  private LoginService loginService;

  public String getLoginId() {
    return loginId;
  }

  public boolean isLoggedIn() {

    if (loginId == null) {
      loginId = loginService.getLoginId();
    }

    return loginId != null;

  }

  public void login() {
    loginService.login();
  }

  public void logout() {
    loginId = null;
    loginService.logout();
  }

}
