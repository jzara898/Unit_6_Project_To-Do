'use strict';

angular.module('todoListApp')
.controller('mainCtrl', function($scope, Todo, $q) {

  $scope.todos = Todo.query();

  $scope.addTodo = function() {
    var todo = new Todo();
    todo.name = 'New Todo';
    todo.iscompleted = false;
    todo.edited = true;
    $scope.todos.unshift(todo);
    // Start in editing mode
    $scope.editing = true;
  };

  $scope.saveTodos = function() {
    // Force input blur to ensure model is updated
    document.activeElement.blur();

    var savePromises = $scope.todos.map(function(todo) {
      if (!todo.id || todo.edited) {
        return Todo.save(todo).$promise.then(function(savedTodo) {
          // Update the existing todo with saved data
          angular.extend(todo, savedTodo);
          todo.edited = false;
          return todo;
        });
      }
      return $q.when(todo);
    });

    $q.all(savePromises).then(function() {
      $scope.todos = Todo.query();
    });
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