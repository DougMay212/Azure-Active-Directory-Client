angular = require('angular');

angular.module('oauthclient')
    .controller('GreetingController', GreetingController);

GreetingController.$inject = ['$scope', '$state', '$stateParams', 'greetingService'];

function GreetingController($scope, $state, $stateParams, greetingService) {

    'use strict';

    var self = this;
    $scope.name = $stateParams.name;
    $scope.message = 'No Greeting';

    $scope.$on('$viewContentLoaded', function () {
        console.log('GreetingController:viewContentLoaded');
        self.onRetrieveGreeting();
    });

    self.onRetrieveGreeting = function () {
        if($stateParams !== $scope.name) {
            $state.go('.', {name: $scope.name}, {notify: false});
        }
        greetingService.getMessage($scope.name)
            .then(function(message) {
                $scope.message = message ? message.value : message;
            });
    };
}