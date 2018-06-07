angular = require('angular');

angular.module('oauthclient')
    .service('greetingService', GreetingService);

function GreetingService($http) {

    'use strict';

    var GET_GREETING_URL = '/greeting';

    var greetingService = {
        getMessage: getMessage
    };

    return greetingService;

    function getMessage(name) {
        return $http.get(GET_GREETING_URL +
                (name === undefined ? '' : '?name=' + name))
            .then(responseHandler)
            .catch(function (errorMessage) {
                console.log('Unable to retrieve message - ' + errorMessage);
            });

        function responseHandler(response) {
            console.log('Response Status: ' + response.status);
            return response.data;
        }
    }
}