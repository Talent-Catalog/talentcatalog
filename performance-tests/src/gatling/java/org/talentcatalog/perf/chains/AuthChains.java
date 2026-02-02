package org.talentcatalog.perf.chains;

import static io.gatling.javaapi.core.CoreDsl.doIf;
import static io.gatling.javaapi.core.CoreDsl.exec;

import io.gatling.javaapi.core.ChainBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talentcatalog.perf.requests.http.AuthRequests;

public final class AuthChains {

  private static final Logger log = LoggerFactory.getLogger(AuthChains.class);

  public static final String SESSION_ACCESS_TOKEN = "accessToken";
  public static final String SESSION_LOGIN_BODY = "loginBody";

  private AuthChains() {}

  /**
   * Ensures the virtual user is logged in (idempotent).
   *
   * <p>If {@code accessToken} is already in session, this does nothing.
   * Otherwise it performs login and fails the user if the token is missing.
   */
  public static ChainBuilder ensureLoggedIn() {
    return doIf(session -> !session.contains(SESSION_ACCESS_TOKEN)).then(
        exec(AuthRequests.login())
            .exec(session -> {
              if (!session.contains(SESSION_ACCESS_TOKEN)) {
                String body = session.contains(SESSION_LOGIN_BODY)
                    ? session.getString(SESSION_LOGIN_BODY)
                    : "<no loginBody>";
                log.warn("LOGIN FAILED: {}", body);
                return session.markAsFailed();
              }
              return session;
            })
            .exitHereIfFailed()
    );
  }
}
