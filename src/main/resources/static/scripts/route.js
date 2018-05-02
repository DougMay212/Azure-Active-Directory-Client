angular = require('angular');

angular
    .module('oauthclient')
    .config(routes);

routes.$inject = ['$stateProvider', '$urlRouterProvider'];

function routes($stateProvider, $urlRouterProvider) {
    'use-strict';
    $urlRouterProvider.otherwise('/');

    $stateProvider
        .state('login', {
            url: '/',
            views: {
                'content': {
                    templateUrl: '/views/landing-page.html',
                    controller: 'AppController',
                    controllerAs: 'ctrl'
                }
            }
        })
        .state('greeting', {
            url: '/greeting?name',
            views: {
                'content@': {
                    templateUrl: '/views/greeting.html',
                    controller: 'GreetingController',
                    controllerAs: 'ctrl',
                    params: {
                        name: {value: 'World', dynamic: true}
                    }
                }
            }
        });
}