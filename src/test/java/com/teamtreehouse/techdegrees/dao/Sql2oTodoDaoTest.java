package com.teamtreehouse.techdegrees.dao;

import com.teamtreehouse.techdegrees.model.Todo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class Sql2oTodoDaoTest {
    private Sql2oTodoDao dao;
    private Connection conn;
    private Sql2o sql2o;

    @BeforeEach
    void setUp() {
        String jdbcUrl = "jdbc:h2:mem:testing;INIT=RUNSCRIPT FROM 'classpath:db/init.sql'";
        sql2o = new Sql2o(jdbcUrl, "", "");
        dao = new Sql2oTodoDao(sql2o);
        conn = sql2o.open();
    }

    @AfterEach
    void tearDown() {
        try (Connection con = sql2o.open()) {
            con.createQuery("DROP TABLE IF EXISTS todos").executeUpdate();
        }
        conn.close();
    }

    @Test
    void creatingTodoSetsId() {
        Todo todo = new Todo("Test Todo");
        int originalId = todo.getId();

        dao.create(todo);

        assertNotEquals(originalId, todo.getId());
    }

    @Test
    void existingTodosCanBeFoundById() {
        Todo todo = new Todo("Test Todo");
        dao.create(todo);

        Todo foundTodo = dao.findById(todo.getId());

        assertEquals(todo.getName(), foundTodo.getName());
        assertEquals(todo.isCompleted(), foundTodo.isCompleted());
    }

    @Test
    void addedTodosAreReturnedFromFindAll() {
        Todo todo = new Todo("Test Todo");
        dao.create(todo);

        List<Todo> todos = dao.findAll();

        assertAll(
                () -> assertNotNull(todos),
                () -> assertEquals(1, todos.size()),
                () -> assertEquals(todo.getName(), todos.get(0).getName())
        );
    }

    @Test
    void noTodosReturnsEmptyList() {
        List<Todo> todos = dao.findAll();
        assertEquals(0, todos.size());
    }

    @Test
    void updateExistingTodoChangesProperties() {
        Todo todo = new Todo("Test Todo");
        dao.create(todo);

        todo.setName("Updated Name");
        todo.setCompleted(true);
        Todo updatedTodo = dao.update(todo);

        assertAll(
                () -> assertEquals("Updated Name", updatedTodo.getName()),
                () -> assertTrue(updatedTodo.isCompleted())
        );
    }

    @Test
    void deletingTodoRemovesIt() {
        Todo todo = new Todo("Test Todo");
        dao.create(todo);

        dao.delete(todo.getId());

        List<Todo> todos = dao.findAll();
        assertEquals(0, todos.size());
    }

    @Test
    void persistenceOfTodosBetweenDAO() {
        // Create a second DAO instance
        Sql2oTodoDao secondDao = new Sql2oTodoDao(sql2o);

        // Create todo with first DAO
        Todo todo = new Todo("Test Todo");
        dao.create(todo);

        // Verify it can be found with second DAO
        Todo foundTodo = secondDao.findById(todo.getId());
        assertNotNull(foundTodo);
        assertEquals(todo.getName(), foundTodo.getName());
    }
}