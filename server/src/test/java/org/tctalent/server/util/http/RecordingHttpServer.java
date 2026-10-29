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

package org.tctalent.server.util.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.lang.Nullable;

/**
 * Minimal real HTTP server for tests which records every request it receives and replies with
 * scripted JSON responses.
 * <p>
 * Used to verify the exact JSON that our HTTP clients put on the wire, and how they decode the
 * JSON they receive, without depending on any particular JSON library in the test itself. This
 * lets the same tests verify behavior across JSON library upgrades (eg Jackson 2 to Jackson 3).
 * </p>
 * <p>
 * Each response is used for one request, in order. Once the responses run out, the last one is
 * repeated.
 * </p>
 */
public class RecordingHttpServer implements AutoCloseable {

    /**
     * A request received by the server.
     */
    public record RecordedRequest(
        String method, String uri, @Nullable String contentType, String body) {

        public String decodedUri() {
            return URLDecoder.decode(uri, StandardCharsets.UTF_8);
        }
    }

    /**
     * A scripted response. A null body means an empty response.
     */
    public record Response(int status, @Nullable String body) {}

    private final List<Response> responses;
    private final List<RecordedRequest> requests = Collections.synchronizedList(new ArrayList<>());
    private final HttpServer server;

    private RecordingHttpServer(List<Response> responses) throws IOException {
        if (responses.isEmpty()) {
            throw new IllegalArgumentException("At least one response is required");
        }
        this.responses = responses;
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/", this::handle);
        server.start();
    }

    public static RecordingHttpServer start(Response... responses) throws IOException {
        return new RecordingHttpServer(List.of(responses));
    }

    public static Response json(int status, String body) {
        return new Response(status, body);
    }

    public static Response empty(int status) {
        return new Response(status, null);
    }

    /**
     * Base URL of the server, ending in "/".
     */
    public String baseUrl() {
        return "http://localhost:" + server.getAddress().getPort() + "/";
    }

    public List<RecordedRequest> requests() {
        synchronized (requests) {
            return List.copyOf(requests);
        }
    }

    public RecordedRequest lastRequest() {
        List<RecordedRequest> all = requests();
        if (all.isEmpty()) {
            throw new IllegalStateException("No requests received");
        }
        return all.get(all.size() - 1);
    }

    private void handle(HttpExchange exchange) throws IOException {
        int index;
        synchronized (requests) {
            index = Math.min(requests.size(), responses.size() - 1);
            requests.add(new RecordedRequest(
                exchange.getRequestMethod(),
                exchange.getRequestURI().toString(),
                exchange.getRequestHeaders().getFirst("Content-Type"),
                new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8)));
        }

        Response response = responses.get(index);
        if (response.body() == null) {
            exchange.sendResponseHeaders(response.status(), -1);
        } else {
            byte[] bytes = response.body().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(response.status(), bytes.length);
            exchange.getResponseBody().write(bytes);
        }
        exchange.close();
    }

    @Override
    public void close() {
        server.stop(0);
    }
}
