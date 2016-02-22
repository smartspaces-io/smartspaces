(function() {
  var smartSpacesWebAdminApp = angular.module(
      'smartSpacesWebAdminApp', ['ngRoute',
        'smartSpacesWebAdminControllers', 'LiveActivityDirective']);

  smartSpacesWebAdminApp
      .config([
        '$routeProvider',
        function($routeProvider) {
          $routeProvider
              .when('/activity', {
                templateUrl: '/smartspaces/static/partials/activity-main.html',
                controller: 'ActivityMainCtrl'
              })
              .when('/activity/:activityId', {
                templateUrl: '/smartspaces/static/partials/activity-detail.html',
                controller: 'ActivityDetailCtrl'
              })
              .when('/liveactivity', {
                templateUrl: '/smartspaces/static/partials/live-activity-main.html',
                controller: 'LiveActivityMainCtrl'
              })
              .when('/liveactivity/:liveActivityId', {
                templateUrl: '/smartspaces/static/partials/live-activity-detail.html',
                controller: 'LiveActivityDetailCtrl'
              })
              .when('/liveactivitygroup', {
                templateUrl: '/smartspaces/static/partials/live-activity-group-main.html',
                controller: 'LiveActivityGroupMainCtrl'
              })
              .when('/liveactivitygroup/:liveActivityGroupId', {
                templateUrl: '/smartspaces/static/partials/live-activity-group-detail.html',
                controller: 'LiveActivityGroupDetailCtrl'
              })
              .when('/space', {
                templateUrl: '/smartspaces/static/partials/space-main.html',
                controller: 'SpaceMainCtrl'
              })
              .when('/space/:spaceId', {
                templateUrl: '/smartspaces/static/partials/space-detail.html',
                controller: 'SpaceDetailCtrl'
              })
              .when('/spacecontroller', {
                templateUrl: '/smartspaces/static/partials/space-controller-main.html',
                controller: 'SpaceControllerMainCtrl'
              })
              .when('/spacecontroller/:spaceControllerId', {
                templateUrl: '/smartspaces/static/partials/space-controller-detail.html',
                controller: 'SpaceControllerDetailCtrl'
              })
              .otherwise({
                redirectTo: '/liveactivity'
              });
        }]);
})();
