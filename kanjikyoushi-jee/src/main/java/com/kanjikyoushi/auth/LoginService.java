package com.kanjikyoushi.auth;

public interface LoginService {

  String getLoginId();

  /**
   * authenticate user
   */
  void login();

  void logout();

}
