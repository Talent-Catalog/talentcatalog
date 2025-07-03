package org.tctalent.server.integration.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.zip.GZIPInputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.MountableFile;
import org.testcontainers.utility.TestcontainersConfiguration;

@Slf4j
public class PostgresTestContainer {

  private static final String DB_NAME = "tctalent";
  private static final String DB_USER = "tctalent";
  private static final String DB_PASSWORD = "tctalent";
  public static final String DEFAULT_DUMP_PATH = "/dump.sql.gz";
  public static final String ENV_DUMP_PATH_KEY = "testcontainers.dump.location";
  public static final String ENV_CONTAINER_MOUNT_KEY = "testcontainers.container.mount";
  private static final String CONTAINER_SQL_PATH = "/dump.sql"; // Use .sql extension

  @Container
  public static final PostgreSQLContainer<?> container = new PostgreSQLContainer<>(getImage())
      .withDatabaseName(DB_NAME)
      .withUsername(DB_USER)
      .withPassword(DB_PASSWORD)
      .withReuse(true);

  static {
    log.info("Initializing PostgreSQL Testcontainer...");
    log.info("Using dump path: {}", getDumpPath());
    log.info("Mount path inside container: {}", getContainerMountPath());
  }

  public static void startContainer() throws IOException, InterruptedException {
    container.start();
    createSchema();
    copyDumpFile();
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

  private static void copyDumpFile() throws IOException {
    log.info("Extracting and transferring dump file to container...");

    // Extract dump.sql.gz to a temporary .sql file
    String extractedDumpPath = extractGzDumpToTemp();

    // Copy the extracted SQL dump file to the container with .sql extension
    container.copyFileToContainer(MountableFile.forHostPath(extractedDumpPath), CONTAINER_SQL_PATH);

    log.info("Dump file transfer complete.");
  }

  private static void importDump() throws IOException, InterruptedException {
    log.info("Running SQL dump inside the container...");
    try {
      container.execInContainer(psqlImportCommand());
    } catch (IOException | InterruptedException e) {
      log.error("Failed to import dump: {}", e.getMessage());
      if (e instanceof InterruptedException) throw e;
    }
    log.info("Dump import complete. JDBC URL: {}", container.getJdbcUrl());
    log.info("Database is ready: {}", container.isRunning());
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
        .getEnvVarOrProperty(ENV_CONTAINER_MOUNT_KEY, CONTAINER_SQL_PATH);
  }

  private static String[] psqlImportCommand() {
    return new String[]{"psql", "-d", DB_NAME, "-U", DB_USER, "-f", CONTAINER_SQL_PATH};
  }

  private static String extractGzDumpToTemp() throws IOException {
    String gzDumpPath = getDumpPath(); // e.g., server/src/test/resources/dump.sql.gz
    File gzFile = new File(gzDumpPath);
    if (!gzFile.exists()) {
      throw new IOException("Dump gzip file not found: " + gzDumpPath);
    }

    // Create temp file for extracted SQL
    File tempSqlFile = Files.createTempFile("dump", ".sql").toFile();
    tempSqlFile.deleteOnExit();

    try (
        FileInputStream fis = new FileInputStream(gzFile);
        GZIPInputStream gis = new GZIPInputStream(fis);
        FileOutputStream fos = new FileOutputStream(tempSqlFile)
    ) {
      byte[] buffer = new byte[8192];
      int len;
      while ((len = gis.read(buffer)) > 0) {
        fos.write(buffer, 0, len);
      }
    }

    log.info("Extracted dump.sql.gz to temporary file: {}", tempSqlFile.getAbsolutePath());
    return tempSqlFile.getAbsolutePath();
  }
}