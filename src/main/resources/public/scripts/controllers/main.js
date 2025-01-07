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
    var savePromises = $scope.todos.map(function(todo) {
      if (!todo.id) {
        return Todo.save({}, todo).$promise;
      } else if (todo.edited) {
        return Todo.update({id: todo.id}, todo).$promise;
      }
      return $q.when(todo); // Return resolved promise for unchanged todos
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