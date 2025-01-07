package com.teamtreehouse.techdegrees;

import com.google.gson.Gson;
import com.teamtreehouse.techdegrees.dao.Sql2oTodoDao;
import com.teamtreehouse.techdegrees.model.Todo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import spark.Spark;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class ApiTest {
    private static final String PORT = "4567";
    private static final String HOST = "http://localhost:" + PORT;
    private static final Gson gson = new Gson();
    private Sql2o sql2o;
    private Connection conn;

    @BeforeEach
    void setUp() {
        // Use in-memory database for testing
        String jdbcUrl = "jdbc:h2:mem:testing;INIT=RUNSCRIPT FROM 'classpath:db/init.sql'";
        sql2o = new Sql2o(jdbcUrl, "", "");
        conn = sql2o.open();

        // Clear any existing data
        conn.createQuery("DELETE FROM todos").executeUpdate();

        // Start the application with our test database
        String[] args = new String[]{jdbcUrl};
        App.main(args);
        Spark.awaitInitialization();
    }

    @AfterEach
    void tearDown() {
        // Clean up database
        conn.createQuery("DELETE FROM todos").executeUpdate();
        conn.close();

        // Stop Spark
        Spark.stop();
        Spark.awaitStop();
    }

    @Test
    void getAllTodosReturnsSuccessfully() throws IOException {
        TestResponse res = request("GET", "/api/v1/todos");
        assertEquals(200, res.status);
    }

    @Test
    void createTodoReturnsCreatedStatus() throws IOException {
        Todo newTodo = new Todo("Test Todo");
        TestResponse res = request("POST", "/api/v1/todos", newTodo);

        assertEquals(201, res.status);
        assertNotNull(res.body, "Response body should not be null");
        Todo created = gson.fromJson(res.body, Todo.class);
        assertNotNull(created, "Created todo should not be null");
        assertNotNull(created.getId(), "Created todo should have an ID");
        assertEquals(newTodo.getName(), created.getName());
    }

    @Test
    void todoPersistedAfterCreation() throws IOException {
        // Create a new todo
        Todo newTodo = new Todo("Persistence Test");
        TestResponse createRes = request("POST", "/api/v1/todos", newTodo);
        assertNotNull(createRes.body, "Creation response body should not be null");
        Todo created = gson.fromJson(createRes.body, Todo.class);
        assertNotNull(created, "Created todo should not be null");

        // Get all todos and verify it exists
        TestResponse getAllRes = request("GET", "/api/v1/todos");
        assertNotNull(getAllRes.body, "GET response body should not be null");
        Todo[] todos = gson.fromJson(getAllRes.body, Todo[].class);
        assertNotNull(todos, "Todos array should not be null");

        boolean found = false;
        for (Todo todo : todos) {
            if (todo.getId() == created.getId()) {
                found = true;
                assertEquals(newTodo.getName(), todo.getName());
                break;
            }
        }
        assertTrue(found, "Created todo was not found in subsequent GET request");
    }

    @Test
    void deleteTodoRemovesItPermanently() throws IOException {
        // First create a todo
        Todo newTodo = new Todo("Delete Test");
        TestResponse createRes = request("POST", "/api/v1/todos", newTodo);
        assertNotNull(createRes.body, "Creation response body should not be null");
        Todo created = gson.fromJson(createRes.body, Todo.class);
        assertNotNull(created, "Created todo should not be null");

        // Delete it
        TestResponse deleteRes = request("DELETE", "/api/v1/todos/" + created.getId());
        assertEquals(204, deleteRes.status);

        // Try to get all todos and verify it's gone
        TestResponse getAllRes = request("GET", "/api/v1/todos");
        assertNotNull(getAllRes.body, "GET response body should not be null");

        if (!getAllRes.body.isEmpty()) {
            Todo[] todos = gson.fromJson(getAllRes.body, Todo[].class);
            assertNotNull(todos, "Todos array should not be null");
            for (Todo todo : todos) {
                assertNotEquals(created.getId(), todo.getId(),
                        "Deleted todo should not be found in subsequent GET request");
            }
        }
    }

    private TestResponse request(String method, String path) throws IOException {
        return request(method, path, null);
    }

    private TestResponse request(String method, String path, Object body) throws IOException {
        URL url = new URL(HOST + path);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setRequestProperty("Content-Type", "application/json");

        // Only set doOutput when we have a body to send
        if (body != null) {
            connection.setDoOutput(true);
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = gson.toJson(body).getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
        }

        // Read the response
        String responseBody = "";
        int responseCode = connection.getResponseCode();

        // Don't try to read the body for 204 responses
        if (responseCode != 204) {
            try (Scanner scanner = new Scanner(
                    responseCode >= 400 ? connection.getErrorStream() : connection.getInputStream(),
                    StandardCharsets.UTF_8.name())) {
                responseBody = scanner.hasNext() ? scanner.useDelimiter("\\A").next() : "";
            }
        }

        return new TestResponse(responseCode, responseBody);
    }

    private static class TestResponse {
        public final String body;
        public final int status;

        public TestResponse(int status, String body) {
            this.status = status;
            this.body = body != null ? body : "";
        }
    }
}