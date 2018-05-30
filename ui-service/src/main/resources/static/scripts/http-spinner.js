angular = require('angular');

angular.module('spinner', [])
    .factory('httpSpinner', HttpSpinner);

function HttpSpinner($q, $rootScope) {
    var numRequests = 0;
    return {
        'request': function(config) {
            numRequests++;
            if(!$rootScope.showSpinner) {
                $rootScope.showSpinner = true;
            }
            return config;
        },
        'response': function(response) {
            if((--numRequests) == 0 && $rootScope.showSpinner) {
                $rootScope.showSpinner = false;
            }
            return response;
        },
        'responseError': function (rejection) {
            if((--numRequests) == 0 && $rootScope.showSpinner) {
                $rootScope.showSpinner = false;
            }
            return $q.reject(rejection);
        }
    };
}