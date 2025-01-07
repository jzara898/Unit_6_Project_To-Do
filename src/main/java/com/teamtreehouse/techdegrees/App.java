package com.teamtreehouse.techdegrees;

import com.google.gson.Gson;
import com.teamtreehouse.techdegrees.dao.Sql2oTodoDao;
import com.teamtreehouse.techdegrees.dao.TodoDao;
import com.teamtreehouse.techdegrees.model.Todo;
import org.sql2o.Sql2o;

import java.io.File;
import static spark.Spark.*;

public class App {
    public static void main(String[] args) {
        staticFileLocation("/public");

        // Database configuration for persistent storage
        String connectionString;
        if (args != null && args.length > 0 && args[0].contains(":mem:")) {
            // Use in-memory database if specified (for tests)
            connectionString = args[0];
        } else {
            // Ensure data directory exists
            File dataDir = new File("data");
            if (!dataDir.exists()) {
                System.out.println("Creating data directory: " + dataDir.getAbsolutePath());
                if (!dataDir.mkdir()) {
                    System.err.println("Failed to create data directory!");
                }
            }

            // Use absolute path for database file
            File dbFile = new File(dataDir, "todos").getAbsoluteFile();
            // Use simpler connection string with just DB_CLOSE_DELAY
            connectionString = "jdbc:h2:" + dbFile.getPath() + ";DB_CLOSE_DELAY=-1";
            System.out.println("Using database at: " + connectionString);
        }

        // Initialize database connection
        Sql2o sql2o = new Sql2o(connectionString, "", "");
        TodoDao todoDao = new Sql2oTodoDao(sql2o);
        Gson gson = new Gson();

        // Set up CORS headers
        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "GET,PUT,POST,DELETE,OPTIONS");
            response.header("Access-Control-Allow-Headers", "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin,");
            response.type("application/json");
        });

        // Set up API routes
        path("/api/v1", () -> {
            // Handle OPTIONS requests
            options("/*", (request, response) -> {
                String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
                if (accessControlRequestHeaders != null) {
                    response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
                }
                String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
                if (accessControlRequestMethod != null) {
                    response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
                }
                return "OK";
            });

            // Get all todos
            get("/todos", (req, res) -> {
                return gson.toJson(todoDao.findAll());
            });

            // Create new todo
            post("/todos", (req, res) -> {
                Todo todo = gson.fromJson(req.body(), Todo.class);
                Todo savedTodo = todoDao.create(todo);
                res.status(201);
                return gson.toJson(savedTodo);
            });

            // Update todo
            put("/todos/:id", (req, res) -> {
                int id = Integer.parseInt(req.params("id"));
                Todo todo = gson.fromJson(req.body(), Todo.class);
                todo.setId(id);
                Todo updatedTodo = todoDao.update(todo);
                return gson.toJson(updatedTodo);
            });

            // Delete todo
            delete("/todos/:id", (req, res) -> {
                todoDao.delete(Integer.parseInt(req.params("id")));
                res.status(204);
                return "";
            });
        });
    }
}