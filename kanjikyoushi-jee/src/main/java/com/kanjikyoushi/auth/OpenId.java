package com.kanjikyoushi.auth;

import java.io.IOException;
import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.openid4java.OpenIDException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.AuthSuccess;
import org.openid4java.message.ParameterList;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.message.ax.FetchResponse;

/**
 * Authenticate user against an openid provider
 */
@Named("openid")
@SessionScoped
public class OpenId implements Serializable {

  private static final String GOOGLE_OPEN_ID = "https://www.google.com/accounts/o8/id";
  private static final long serialVersionUID = -3729911752063723004L;

  @Inject
  private FacesContext context;
  private DiscoveryInformation discovered;
  @Inject
  private transient Logger LOG;
  private boolean loggedIn;
  private ConsumerManager manager;
  private String openIdEmail;
  /**
   * Users OpenID URL
   */
  private String userSuppliedId = GOOGLE_OPEN_ID;
  private String validatedId;

  public OpenId() {
  }

  /**
   * Create an authentication request. It performs a discovery on the
   * user-supplied identifier. Attempt it to associate with the OpenID provider
   * and retrieve one service endpoint for authentication. It adds some
   * attributes for exchange on the AuthRequest. A List of all possible
   * attributes can be found on @see http://www.axschema.org/types/
   * 
   * @param returnToUrl
   * @return the URL where the message should be sent
   */
  private String authRequest(String returnToUrl) {

    LOG.info("checking login status");
    String requestUrl = null;

    try {
      discovered = manager.associate(manager.discover(userSuppliedId));
      AuthRequest authReq = manager.authenticate(discovered, returnToUrl);

      FetchRequest fetch = FetchRequest.createFetchRequest();
      fetch.addAttribute("email", "http://schema.openid.net/contact/email",
          true);
      /* Some other attributes ... */

      authReq.addExtension(fetch);
      requestUrl = authReq.getDestinationUrl(true);
    } catch (OpenIDException e) {
      LOG.error(e, e);
    }

    LOG.info("created authentication request: " + requestUrl);
    return requestUrl;

  }

  private HttpServletRequest getHttpRequest() {
    return (HttpServletRequest) context.getExternalContext().getRequest();
  }

  public String getOpenIdEmail() {
    return openIdEmail;
  }

  public String getValidatedId() {
    return validatedId;
  }

  private void initialize() {
    LOG.info("initializing open id");
    manager = null;
    discovered = null;
    validatedId = null;
    userSuppliedId = null;
    loggedIn = false;
  }

  public synchronized boolean isLoggedIn() {
    if (!loggedIn) {
      if (manager == null) {
        loggedIn = false;
      } else {
        if (validatedId == null) {
          verify();
        }
        loggedIn = validatedId != null && !validatedId.isEmpty();
        LOG.info("login status: "
            + (loggedIn ? "logged in as " + openIdEmail : "not logged in"));
      }
    }
    return loggedIn;
  }

  public void login() {
    LOG.info("logging in");
    manager = new ConsumerManager();
    validatedId = null;
    String returnToUrl = returnToUrl();
    try {
      String url = authRequest(returnToUrl);
      if (url != null) {
        context.getExternalContext().redirect(url);
        LOG.info("logged in");
      }
    } catch (IOException e) {
      LOG.error(e, e);
      initialize();
    }
  }

  public void logout() {
    initialize();
  }

  private String returnToUrl() {
    String returnToUrl = getHttpRequest().getRequestURL().toString();
    return returnToUrl;
  }

  public void setUserSuppliedId(String userSuppliedId) {
    this.userSuppliedId = userSuppliedId;
  }

  public void setValidatedId(String validatedId) {
    this.validatedId = validatedId;
  }

  private synchronized void verify() {
    validatedId = verifyResponse(getHttpRequest());
  }

  /**
   * Set the class members with data from the authentication response. Extract
   * the parameters from the authentication response (which comes in as a HTTP
   * request from the OpenID provider). Verify the response, examine the
   * verification result and extract the verified identifier.
   * 
   * @param httpReq
   *          httpRequest
   * @return users identifier.
   */
  private String verifyResponse(HttpServletRequest httpReq) {

    LOG.info("verify response");
    String userIdentifier = null;

    try {
      ParameterList response = new ParameterList(httpReq.getParameterMap());

      StringBuffer receivingURL = httpReq.getRequestURL();
      String queryString = httpReq.getQueryString();
      if (queryString != null && queryString.length() > 0) {
        receivingURL.append("?").append(httpReq.getQueryString());
      }

      VerificationResult verification = manager.verify(receivingURL.toString(),
          response, discovered);

      Identifier verified = verification.getVerifiedId();
      if (verified != null) {
        userIdentifier = verified.getIdentifier();
        AuthSuccess authSuccess = (AuthSuccess) verification.getAuthResponse();
        if (authSuccess.hasExtension(AxMessage.OPENID_NS_AX)) {
          FetchResponse fetchResp = (FetchResponse) authSuccess
              .getExtension(AxMessage.OPENID_NS_AX);
          openIdEmail = fetchResp.getAttributeValue("email");
          /* Some other attributes ... */
        }
      }
    } catch (OpenIDException e) {
      LOG.error("error verifying response: " + e, e);
    }

    LOG.info("verified response for account: " + userIdentifier);
    return userIdentifier;

  }
}
