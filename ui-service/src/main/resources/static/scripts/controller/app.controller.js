angular = require('angular');

angular.module('oauthclient')
    .controller('AppController', AppController);

AppController.$inject = ['$scope'];

function AppController($scope) {

    'use strict';

    $scope.$on('$viewContentLoaded', function () {
        console.log('AppController:viewContentLoaded');
    });
}