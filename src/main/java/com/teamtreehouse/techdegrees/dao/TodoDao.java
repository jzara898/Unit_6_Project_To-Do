package com.teamtreehouse.techdegrees.dao;

import com.teamtreehouse.techdegrees.model.Todo;

import java.util.List;

public interface TodoDao {
    List<Todo> findAll();
    Todo findById(int id);
    Todo create(Todo todo);
    Todo update(Todo todo);
    void delete(int id);
}