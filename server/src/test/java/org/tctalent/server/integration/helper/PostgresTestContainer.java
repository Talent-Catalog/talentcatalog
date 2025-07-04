package org.tctalent.server.integration.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.MountableFile;
import org.testcontainers.utility.TestcontainersConfiguration;

/**
 * Provides a reusable PostgreSQL Testcontainer instance preloaded with a SQL dump.
 * This setup ensures the database is ready with expected schema and data before
 * Spring Boot initializes.
 *
 * Configuration Notes:
 * - Requires `testcontainers.properties` to be on the classpath.
 * - Set `TCTALENT_DB_HOME` or override via `testcontainers.dump.location`.
 * - Ensure the SQL dump path starts with a `/` (e.g., `/my-dump.sql`).
 */
@Slf4j
public class PostgresTestContainer {

  private static final String DB_NAME = "tctalent";
  private static final String DB_USER = "tctalent";
  private static final String DB_PASSWORD = "tctalent";
  public static final String DEFAULT_DUMP_PATH = "/dump.sql";
  public static final String ENV_DUMP_PATH_KEY = "testcontainers.dump.location";
  public static final String ENV_CONTAINER_MOUNT_KEY = "testcontainers.container.mount";

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

  /**
   * Starts the PostgreSQL container and prepares the schema and users.
   */
  public static void startContainer() throws IOException, InterruptedException {
    long start = System.currentTimeMillis();
    log.info("🟡 Starting PostgreSQL container...");
    container.start();
    log.info("🟢 Container started in {} ms", System.currentTimeMillis() - start);

    createSchema();
    copyDumpFile();
    importDump();
  }


  /**
   * Executes SQL commands to create the target database and users.
   */
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

  /**
   * Copies the SQL dump into the container.
   */
  private static void copyDumpFile() throws IOException {
    log.info("Preparing dump file for import...");

    File extractedDump = decompressGzToTempFile(getDumpPath());

    log.info("Transferring SQL dump to container...");
    container.copyFileToContainer(
        MountableFile.forHostPath(extractedDump.getAbsolutePath()),
        getContainerMountPath()
    );
    log.info("✅ SQL dump file copied to container at {}", getContainerMountPath());
  }



  /**
   * Executes the SQL dump inside the container to preload schema and data.
   */
  private static void importDump() throws IOException, InterruptedException {
    log.info("Running SQL dump inside the container...");

    final int maxAttempts = 5;
    final int delayMillis = 3000;
    boolean success = false;

    for (int attempt = 1; attempt <= maxAttempts && !success; attempt++) {
      log.info("Attempt {} of {} to run psql dump import...", attempt, maxAttempts);
      try {
        // Debug: list files at target location
        log.info("Checking for dump file inside container at {}", getContainerMountPath());
        container.execInContainer("ls", "-lah", getContainerMountPath());

        // Run import
        container.execInContainer(psqlImportCommand());
        success = true;
        log.info("✅ Dump import completed successfully.");
      } catch (Exception e) {
        log.warn("⚠️ Attempt {} failed to import dump: {}", attempt, e.getMessage());
        if (attempt < maxAttempts) {
          log.info("Retrying after {}ms...", delayMillis);
          Thread.sleep(delayMillis);
        } else {
          log.error("❌ Failed to import dump after {} attempts.", maxAttempts);
          if (e instanceof InterruptedException) throw (InterruptedException) e;
          throw new IOException("Failed to import dump after multiple attempts", e);
        }
      }
    }

    log.info("Database is running: {}", container.isRunning());
    log.info("JDBC URL: {}", container.getJdbcUrl());
  }


  /**
   * Provides Spring Boot with the correct datasource properties from the container.
   *
   * @param registry Spring's dynamic property registry
   */
  public static void injectContainerProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", container::getJdbcUrl);
    registry.add("spring.datasource.username", container::getUsername);
    registry.add("spring.datasource.password", container::getPassword);
  }

  // Utility methods

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
        .getEnvVarOrProperty(ENV_CONTAINER_MOUNT_KEY,
            "ERROR: Container mount path not defined");
  }

  private static String[] psqlImportCommand() {
    return new String[]{"psql", "-d", DB_NAME, "-U", DB_USER, "-f", getContainerMountPath()};
  }

  private static File decompressGzToTempFile(String gzPath) throws IOException {
    log.info("Decompressing .gz file from: {}", gzPath);
    File gzFile = new File(gzPath);
    if (!gzFile.exists()) {
      throw new IOException("❌ Dump .gz file not found: " + gzPath);
    }

    File tempFile = File.createTempFile("decompressed-dump", ".sql");
    tempFile.deleteOnExit();

    try (
        FileInputStream fis = new FileInputStream(gzFile);
        java.util.zip.GZIPInputStream gis = new java.util.zip.GZIPInputStream(fis);
        FileOutputStream fos = new FileOutputStream(tempFile)
    ) {
      byte[] buffer = new byte[8192];
      int len;
      while ((len = gis.read(buffer)) > 0) {
        fos.write(buffer, 0, len);
      }
    }

    log.info("✅ Decompressed dump to temp file: {}", tempFile.getAbsolutePath());
    return tempFile;
  }

}
