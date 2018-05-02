angular = require('angular');
require('angular-ui-router');
require('../dist/viewCache');
require('http-spinner');

angular.module('oauthclient', ['ui.router', 'views', 'spinner'])
    .config(function ($httpProvider) {
        $httpProvider.interceptors.push('httpSpinner');
    });

require('route');
require('app.controller');
require('greeting.controller');
require('logout.controller');
require('greeting.service');