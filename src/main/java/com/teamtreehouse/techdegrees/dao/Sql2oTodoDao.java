package com.teamtreehouse.techdegrees.dao;

import com.teamtreehouse.techdegrees.model.Todo;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.sql2o.Sql2oException;

import java.util.List;

public class Sql2oTodoDao implements TodoDao {
    private final Sql2o sql2o;

    public Sql2oTodoDao(Sql2o sql2o) {
        this.sql2o = sql2o;
        try (Connection conn = sql2o.open()) {
            System.out.println("Initializing database...");
            String sql = "CREATE TABLE IF NOT EXISTS todos (" +
                    "id INTEGER PRIMARY KEY AUTO_INCREMENT, " +
                    "name VARCHAR(100) NOT NULL, " +
                    "iscompleted BOOLEAN DEFAULT FALSE" +
                    ")";
            conn.createQuery(sql).executeUpdate();
            System.out.println("Table creation completed");
        } catch (Exception e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public List<Todo> findAll() {
        try (Connection conn = sql2o.open()) {
            List<Todo> todos = conn.createQuery("SELECT * FROM todos")
                    .executeAndFetch(Todo.class);
            System.out.println("Found " + todos.size() + " todos in findAll()");
            return todos;
        }
    }

    @Override
    public Todo findById(int id) {
        try (Connection conn = sql2o.open()) {
            Todo todo = conn.createQuery("SELECT * FROM todos WHERE id = :id")
                    .addParameter("id", id)
                    .executeAndFetchFirst(Todo.class);
            System.out.println("Found to-do with id " + id + ": " + (todo != null));
            return todo;
        }
    }

    @Override
    public Todo create(Todo todo) {
        String sql = "INSERT INTO todos(name, iscompleted) VALUES (:name, :iscompleted)";
        try (Connection conn = sql2o.beginTransaction()) {
            int id = (int) conn.createQuery(sql, true)
                    .addParameter("name", todo.getName())
                    .addParameter("iscompleted", todo.isCompleted())
                    .executeUpdate()
                    .getKey();
            todo.setId(id);
            conn.commit();
            System.out.println("Created new to-do with ID: " + id);
            return todo;
        }
    }

    @Override
    public Todo update(Todo todo) {
        String sql = "UPDATE todos SET name = :name, iscompleted = :iscompleted WHERE id = :id";
        try (Connection conn = sql2o.beginTransaction()) {
            conn.createQuery(sql)
                    .addParameter("name", todo.getName())
                    .addParameter("iscompleted", todo.isCompleted())
                    .addParameter("id", todo.getId())
                    .executeUpdate();
            conn.commit();
            System.out.println("Updated todo with ID: " + todo.getId());
            return findById(todo.getId());
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM todos WHERE id = :id";
        try (Connection conn = sql2o.beginTransaction()) {
            conn.createQuery(sql)
                    .addParameter("id", id)
                    .executeUpdate();
            conn.commit();
            System.out.println("Deleted to-do with ID: " + id);
        }
    }

    public void clearAllTodos() {
        String sql = "DELETE FROM todos";
        try (Connection conn = sql2o.beginTransaction()) {
            conn.createQuery(sql).executeUpdate();
            conn.commit();
            System.out.println("Cleared all to-dos");
        }
    }
}