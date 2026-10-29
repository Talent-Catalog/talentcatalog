/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.integration.cache;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.tctalent.server.integration.helper.BaseDBIntegrationTest;
import org.tctalent.server.integration.helper.TestDataFactory;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.UserRepository;
import org.tctalent.server.repository.db.read.cache.CandidateRedisCache;
import org.tctalent.server.repository.db.read.cache.CandidateRedisCache.VersionedJson;

/**
 * Tests our use of Redis against a real Redis server (as in production, and unlike the other
 * tests, which turn caching off and mock the candidate cache): the "users" Spring cache, the
 * fallback to the database when a cached value cannot be read (eg one written by a previous
 * version of the application), and the candidate JSON cache.
 * <p>
 * Added in TC-1189 (Spring Data Redis 4, Lettuce 7).
 * </p>
 */
@SpringBootTest(properties = {
    //Undo application-test.yml, which turns off Redis and caching
    "spring.autoconfigure.exclude=",
    "spring.cache.type=redis"
})
class RedisIntegrationTest extends BaseDBIntegrationTest {

    private static final GenericContainer<?> redis =
        new GenericContainer<>("redis:7.2.4").withExposedPorts(6379);

    static {
        redis.start();
    }

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @AfterAll
    static void stopRedis() {
        redis.stop();
    }

    @Autowired private UserRepository userRepository;
    @Autowired private CandidateRedisCache candidateRedisCache;
    @Autowired private StringRedisTemplate redisTemplate;
    @Autowired private JdbcTemplate jdbc;

    private User user;

    @BeforeEach
    void setUp() {
        String name = "tc1189_redis_" + UUID.randomUUID();
        user = TestDataFactory.createUser(null);
        user.setUsername(name);
        user.setEmail(name + "@example.com");
        user = userRepository.save(user);
    }

    @AfterEach
    void tearDown() {
        jdbc.update("delete from users where id = ?", user.getId());
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
    }

    @Test
    @DisplayName("users are cached in Redis and read back from it")
    void usersCache() {
        User loaded = userRepository.findByUsernameIgnoreCase(user.getUsername());
        assertThat(loaded.getId()).isEqualTo(user.getId());
        assertThat(redisTemplate.hasKey("users::" + user.getUsername())).isTrue();

        //Remove the row from the database - so the next lookup must come from Redis
        jdbc.update("delete from users where id = ?", user.getId());
        User cached = userRepository.findByUsernameIgnoreCase(user.getUsername());

        assertThat(cached).isNotNull();
        assertThat(cached.getId()).isEqualTo(user.getId());
        assertThat(cached.getUsername()).isEqualTo(user.getUsername());
        assertThat(cached.getPartner().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("a cached user which cannot be read falls through to the database")
    void unreadableCachedUserFallsThroughToDatabase() {
        //Eg a value written by a previous version of the application
        redisTemplate.opsForValue().set("users::" + user.getUsername(), "not a cached user");

        User loaded = userRepository.findByUsernameIgnoreCase(user.getUsername());

        assertThat(loaded).isNotNull();
        assertThat(loaded.getId()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("candidate JSON is stored in, read from and cleared from Redis")
    void candidateJsonCache() {
        candidateRedisCache.putAll(Map.of(
            101L, new VersionedJson(101L, 7L, "{\"id\":101,\"name\":\"café\"}"),
            102L, new VersionedJson(102L, 3L, "{\"id\":102}")));

        assertThat(candidateRedisCache.multiGet(Map.of(101L, 7L, 102L, 3L, 103L, 1L)))
            .containsExactlyInAnyOrderEntriesOf(Map.of(
                101L, "{\"id\":101,\"name\":\"café\"}",
                102L, "{\"id\":102}"));
        //A different version is a cache miss
        assertThat(candidateRedisCache.multiGet(Map.of(101L, 8L))).isEmpty();

        candidateRedisCache.clear();
        assertThat(candidateRedisCache.multiGet(Map.of(101L, 7L, 102L, 3L))).isEmpty();
    }
}
