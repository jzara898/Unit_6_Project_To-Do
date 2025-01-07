'use strict';

angular.module('todoListApp')
.controller('todoCtrl', function($scope) {
    $scope.toggleCompleted = function(todo) {
        todo.iscompleted = !todo.iscompleted;
        todo.edited = true;
    };

    $scope.editing = false;

    $scope.startEditing = function(todo) {
        $scope.editing = true;
        $scope.editingTodo = todo;
    };

    $scope.setEdited = function(todo) {
        todo.edited = true;
    };
});