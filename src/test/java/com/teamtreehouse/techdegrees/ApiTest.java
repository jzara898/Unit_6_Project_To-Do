package com.teamtreehouse.techdegrees;

import com.google.gson.Gson;
import com.teamtreehouse.techdegrees.model.Todo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

    @BeforeEach
    void setUp() {
        App.main(null);
        Spark.awaitInitialization();
    }

    @AfterEach
    void tearDown() {
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
        Todo created = gson.fromJson(res.body, Todo.class);
        assertNotNull(created.getId());
        assertEquals(newTodo.getName(), created.getName());
    }

    @Test
    void todoPersistedAfterCreation() throws IOException {
        // Create a new todo
        Todo newTodo = new Todo("Persistence Test");
        TestResponse createRes = request("POST", "/api/v1/todos", newTodo);
        Todo created = gson.fromJson(createRes.body, Todo.class);

        // Get all todos and verify it exists
        TestResponse getAllRes = request("GET", "/api/v1/todos");
        Todo[] todos = gson.fromJson(getAllRes.body, Todo[].class);

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
    void updateTodoSuccessfully() throws IOException {
        // First create a todo
        Todo newTodo = new Todo("Test Todo");
        TestResponse createRes = request("POST", "/api/v1/todos", newTodo);
        Todo created = gson.fromJson(createRes.body, Todo.class);

        // Update it
        created.setName("Updated Todo");
        created.setCompleted(true);
        TestResponse updateRes = request("PUT", "/api/v1/todos/" + created.getId(), created);

        assertEquals(200, updateRes.status);
        Todo updated = gson.fromJson(updateRes.body, Todo.class);
        assertEquals("Updated Todo", updated.getName());
        assertTrue(updated.isCompleted());
    }

    @Test
    void deleteTodoRemovesItPermanently() throws IOException {
        // Create a todo
        Todo newTodo = new Todo("Delete Test");
        TestResponse createRes = request("POST", "/api/v1/todos", newTodo);
        Todo created = gson.fromJson(createRes.body, Todo.class);

        // Delete it
        TestResponse deleteRes = request("DELETE", "/api/v1/todos/" + created.getId());
        assertEquals(204, deleteRes.status);

        // Try to get it - should not be found
        TestResponse getAllRes = request("GET", "/api/v1/todos");
        Todo[] todos = gson.fromJson(getAllRes.body, Todo[].class);

        for (Todo todo : todos) {
            assertNotEquals(created.getId(), todo.getId(),
                    "Deleted todo should not be found in subsequent GET request");
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
        connection.setDoOutput(true);

        if (body != null) {
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = gson.toJson(body).getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
        }

        connection.connect();
        String responseBody = "";
        try {
            if (connection.getResponseCode() < HttpURLConnection.HTTP_BAD_REQUEST) {
                responseBody = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8.name())
                        .useDelimiter("\\A")
                        .next();
            }
        } catch (IOException e) {
            // If there's no response body, that's ok
        }

        return new TestResponse(connection.getResponseCode(), responseBody);
    }

    private static class TestResponse {
        public final String body;
        public final int status;

        public TestResponse(int status, String body) {
            this.status = status;
            this.body = body;
        }
    }
}