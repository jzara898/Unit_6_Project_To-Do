'use strict';

angular.module('todoListApp')
.controller('todoCtrl', function($scope) {
    $scope.toggleCompleted = function(todo) {
        todo.iscompleted = !todo.iscompleted;
        todo.edited = true;
    };

    $scope.editing = !$scope.todo.id; // Start in editing mode for new todos

    $scope.setEdited = function(todo) {
        todo.edited = true;
    };

    $scope.finishEditing = function(todo) {
        $scope.editing = false;
        todo.edited = true;
    };
});