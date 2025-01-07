package com.teamtreehouse.techdegrees.model;

public class Todo {
    private int id;
    private String name;
    private boolean iscompleted;  // Changed to match H2 database column name (H2 converts to uppercase)

    public Todo(String name) {
        this.name = name;
        this.iscompleted = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isCompleted() {  // Keep getter as isCompleted for normal Java conventions
        return iscompleted;
    }

    public void setCompleted(boolean completed) {  // Keep setter as setCompleted
        this.iscompleted = completed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Todo todo = (Todo) o;

        if (id != todo.id) return false;
        if (iscompleted != todo.iscompleted) return false;
        return name != null ? name.equals(todo.name) : todo.name == null;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (iscompleted ? 1 : 0);
        return result;
    }
}