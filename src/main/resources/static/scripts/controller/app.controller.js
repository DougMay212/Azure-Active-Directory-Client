angular = require('angular');

angular.module('oauthclient')
    .controller('AppController', AppController);

AppController.$inject = ['$scope', '$http'];

function AppController($scope, $http) {

    'use strict';

    $scope.$on('$viewContentLoaded', function () {
        console.log('AppController:viewContentLoaded');
    });

    $scope.logout = function () {
        console.log('Logging out...');
        $http.post('/logout', '').then(function () {
            console.log('Successfully logged out');
            window.location.href = "loggedout";
        });
    }
}