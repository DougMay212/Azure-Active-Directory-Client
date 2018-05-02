angular = require('angular');

angular.module('oauthclient')
    .controller('LogoutController', LogoutController);

LogoutController.$inject = ['$scope', '$http'];

function LogoutController($scope, $http) {

    'use strict';

    var self = this;
    self.name = 'You have successfully logged out.';

    logout();

    $scope.$on('$viewContentLoaded', function () {
        console.log('AppController:viewContentLoaded');
    });

    function logout() {
        console.log('Logging out...');
        $http.post('/logout', '').then(function () {
            console.log('Successfully logged out');
            window.location.href = "loggedout";
        });

    }


}