'use strict';

angular.module('todoListApp')
.controller('mainCtrl', function($scope, Todo, $q) {

  $scope.todos = Todo.query();

  $scope.addTodo = function() {
    var todo = new Todo();
    todo.name = 'New Todo';
    todo.iscompleted = false;
    $scope.todos.unshift(todo);
  };

  $scope.saveTodos = function() {
    // Force model update if an input is active
    if (document.activeElement.tagName === 'INPUT') {
      document.activeElement.blur();
    }

    // Find unsaved todo
    var unsavedTodo = $scope.todos.find(function(todo) {
      return !todo.id;
    });

    if (unsavedTodo) {
      console.log('Saving new todo:', unsavedTodo);
      // For new todos
      Todo.save({}, {
        name: unsavedTodo.name,
        iscompleted: unsavedTodo.iscompleted
      }).$promise.then(function(savedTodo) {
        console.log('Todo saved successfully:', savedTodo);
        // Replace the unsaved todo with the saved version
        var index = $scope.todos.findIndex(function(t) {
          return !t.id;
        });
        if (index !== -1) {
          $scope.todos[index] = savedTodo;
        }
      }).catch(function(error) {
        console.error('Failed to save todo:', error);
      });
    } else {
      // For existing todos
      var editedTodos = $scope.todos.filter(function(todo) {
        return todo.edited && todo.id;
      });

      if (editedTodos.length > 0) {
        var promises = editedTodos.map(function(todo) {
          return Todo.update({ id: todo.id }, todo).$promise;
        });

        $q.all(promises).then(function(results) {
          // Clear edited flag after successful update
          editedTodos.forEach(function(todo) {
            todo.edited = false;
          });
        });
      }
    }
  };

  $scope.deleteTodo = function(todo, index) {
    if (todo.id) {
      Todo.delete({id: todo.id}, function() {
        $scope.todos.splice(index, 1);
      });
    } else {
      $scope.todos.splice(index, 1);
    }
  };

  $scope.refreshTodos = function() {
    $scope.todos = Todo.query();
  };
});