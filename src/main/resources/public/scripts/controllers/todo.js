'use strict';

angular.module('todoListApp')
.controller('todoCtrl', function($scope) {
    $scope.toggleCompleted = function(todo) {
        todo.iscompleted = !todo.iscompleted;
        todo.edited = true;
    };

    $scope.editing = false;

    $scope.setEdited = function(todo) {
        todo.edited = true;
    };
});