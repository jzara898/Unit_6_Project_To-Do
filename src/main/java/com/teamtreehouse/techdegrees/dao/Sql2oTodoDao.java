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
            String sql = "CREATE TABLE IF NOT EXISTS todos (" +
                    "id INTEGER PRIMARY KEY AUTO_INCREMENT, " +
                    "name VARCHAR(100) NOT NULL, " +
                    "iscompleted BOOLEAN DEFAULT FALSE" +  // Changed to match the field name
                    ")";
            conn.createQuery(sql).executeUpdate();
        }
    }

    @Override
    public List<Todo> findAll() {
        try (Connection conn = sql2o.open()) {
            return conn.createQuery("SELECT * FROM todos")
                    .executeAndFetch(Todo.class);
        }
    }

    @Override
    public Todo findById(int id) {
        try (Connection conn = sql2o.open()) {
            return conn.createQuery("SELECT * FROM todos WHERE id = :id")
                    .addParameter("id", id)
                    .executeAndFetchFirst(Todo.class);
        }
    }

    @Override
    public Todo create(Todo todo) {
        String sql = "INSERT INTO todos(name, iscompleted) VALUES (:name, :iscompleted)";
        try (Connection conn = sql2o.open()) {
            int id = (int) conn.createQuery(sql, true)
                    .addParameter("name", todo.getName())
                    .addParameter("iscompleted", todo.isCompleted())
                    .executeUpdate()
                    .getKey();
            todo.setId(id);
            return todo;
        }
    }

    @Override
    public Todo update(Todo todo) {
        String sql = "UPDATE todos SET name = :name, iscompleted = :iscompleted WHERE id = :id";
        try (Connection conn = sql2o.open()) {
            conn.createQuery(sql)
                    .addParameter("name", todo.getName())
                    .addParameter("iscompleted", todo.isCompleted())
                    .addParameter("id", todo.getId())
                    .executeUpdate();
            return findById(todo.getId());
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM todos WHERE id = :id";
        try (Connection conn = sql2o.open()) {
            conn.createQuery(sql)
                    .addParameter("id", id)
                    .executeUpdate();
        }
    }

    public void clearAllTodos() {
        String sql = "DELETE FROM todos";
        try (Connection conn = sql2o.open()) {
            conn.createQuery(sql).executeUpdate();
        }
    }
}