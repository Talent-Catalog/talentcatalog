package org.tctalent.server.integration.helper;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.TestcontainersConfiguration;

/**
 * Provides a reusable PostgreSQL Testcontainer instance preloaded with a SQL dump.
 */
@Slf4j
public class PostgresTestContainer {

  private static final String DB_NAME = "tctalent";
  private static final String DB_USER = "tctalent";
  private static final String DB_PASSWORD = "tctalent";
  public static final String DEFAULT_DUMP_PATH = "src/test/resources/dump.sql.gz";
  public static final String DEFAULT_CONTAINER_MOUNT_PATH = "/docker-entrypoint-initdb.d/dump.sql.gz";
  public static final String ENV_DUMP_PATH_KEY = "testcontainers.dump.location";
  public static final String ENV_CONTAINER_MOUNT_KEY = "testcontainers.container.mount";

  @Container
  public static final PostgreSQLContainer<?> container = new PostgreSQLContainer<>(getImage())
      .withDatabaseName(DB_NAME)
      .withUsername(DB_USER)
      .withPassword(DB_PASSWORD)
      .withFileSystemBind(getDumpPath(), getContainerMountPath(), BindMode.READ_ONLY)
      .withReuse(true);

  static {
    log.info("Initializing PostgreSQL Testcontainer...");
    log.info("Using dump path: {}", getDumpPath());
    log.info("Mount path inside container: {}", getContainerMountPath());
  }

  public static void startContainer() throws IOException, InterruptedException {
    container.start();
    createSchema();
    importDump();
  }

  private static void createSchema() throws IOException, InterruptedException {
    log.info("Creating schema and roles...");
    try {
      container.execInContainer(createDatabaseCommand());
      container.execInContainer(createUserCommand());
      container.execInContainer(grantSuperuserCommand());
    } catch (IOException | InterruptedException e) {
      log.error("Schema initialization failed.", e);
      if (e instanceof InterruptedException) throw e;
    }
    log.info("Schema and roles initialized.");
  }

  private static void importDump() throws IOException, InterruptedException {
    log.info("Importing dump using psql...");
    try {
      org.testcontainers.containers.Container.ExecResult result = container.execInContainer("bash", "-c",
          "psql -U " + DB_USER + " -d " + DB_NAME + " -f " + getContainerMountPath());
      log.info("Dump import output: {}", result.getStdout());
      log.error("Dump import errors (if any): {}", result.getStderr());
      if (result.getExitCode() != 0) {
        throw new IOException("Dump import failed with exit code: " + result.getExitCode());
      }
    } catch (IOException | InterruptedException e) {
      log.error("Failed to import dump: {}", e.getMessage());
      throw e;
    }
    log.info("Dump import complete. JDBC URL: {}", container.getJdbcUrl());
  }

  public static void injectContainerProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", container::getJdbcUrl);
    registry.add("spring.datasource.username", container::getUsername);
    registry.add("spring.datasource.password", container::getPassword);
  }

  private static String getImage() {
    return "postgres:14";
  }

  private static String[] createDatabaseCommand() {
    return new String[]{"psql", "-U", "postgres", "-c", "CREATE DATABASE " + DB_NAME + ";"};
  }

  private static String[] createUserCommand() {
    return new String[]{"psql", "-U", "postgres", "-c",
        "CREATE USER " + DB_USER + " WITH PASSWORD '" + DB_PASSWORD + "';"};
  }

  private static String[] grantSuperuserCommand() {
    return new String[]{"psql", "-U", "postgres", "-c",
        "ALTER USER " + DB_USER + " WITH SUPERUSER;"};
  }

  private static String getDumpPath() {
    return TestcontainersConfiguration.getInstance()
        .getEnvVarOrProperty(ENV_DUMP_PATH_KEY, DEFAULT_DUMP_PATH);
  }

  private static String getContainerMountPath() {
    return TestcontainersConfiguration.getInstance()
        .getEnvVarOrProperty(ENV_CONTAINER_MOUNT_KEY, DEFAULT_CONTAINER_MOUNT_PATH);
  }
}