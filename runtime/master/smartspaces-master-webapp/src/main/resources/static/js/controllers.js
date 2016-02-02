(function() {
  var module = angular.module('smartSpacesWebAdminControllers', []);

  module.controller('ActivityMainCtrl', [
      '$scope',
      '$http',
      function($scope, $http) {
        $http.get('/smartspaces/activity/all.json').success(
            function(data) {
              $scope.activities = data.data;
            });
        
      } ]);

  module.controller('ActivityDetailCtrl', [
      '$scope',
      '$http',
      '$routeParams',
      function($scope, $http, $routeParams) {
        $http.get(
            '/smartspaces/activity/' + $routeParams.activityId
                + '/view.json').success(function(data) {
          $scope.activityInfo = data.data;
          $scope.hasLiveActivities = $scope.activityInfo.liveactivities.length != 0;
        });
        
        this.hasLiveActivities = function() {
          return $scope.activityInfo.liveactivities.length != 0;
        }
      } ]);

  module.controller('LiveActivityMainCtrl', [
      '$scope',
      '$http',
      function($scope, $http) {
        $http.get('/smartspaces/liveactivity/all.json').success(
            function(data) {
              $scope.liveActivities = data.data;
            });
      } ]);

  module.controller('LiveActivityDetailCtrl', [
      '$scope',
      '$http',
      '$routeParams',
      function($scope, $http, $routeParams) {
        $http.get(
            '/smartspaces/liveactivity/' + $routeParams.liveActivityId
                + '/view.json').success(function(data) {
          $scope.liveActivityInfo = data.data;
        });
      } ]);

  module.controller('LiveActivityGroupMainCtrl', [
      '$scope',
      '$http',
      function($scope, $http) {
        $http.get('/smartspaces/liveactivitygroup/all.json').success(
            function(data) {
              $scope.liveActivityGroups = data.data;
            });
      } ]);

  module.controller('LiveActivityGroupDetailCtrl', [
      '$scope',
      '$http',
      '$routeParams',
      function($scope, $http, $routeParams) {
        $http.get(
            '/smartspaces/liveactivitygroup/'
                + $routeParams.liveActivityGroupId + '/view.json').success(
            function(data) {
              $scope.liveActivityGroupInfo = data.data;
            });
      } ]);

  module.controller('SpaceMainCtrl', [ '$scope', '$http',
      function($scope, $http) {
        $http.get('/smartspaces/space/all.json').success(function(data) {
          console.log(data.data);
          $scope.spaces = data.data;
        });
      } ]);

  module.controller('SpaceDetailCtrl', [
      '$scope',
      '$http',
      '$routeParams',
      function($scope, $http, $routeParams) {
        $http.get(
            '/smartspaces/space/' + $routeParams.spaceId + '/view.json')
            .success(function(data) {
              $scope.spaceInfo = data.data;
            });
      } ]);

  module.controller('SpaceControllerMainCtrl', [
      '$scope',
      '$http',
      function($scope, $http) {
        $http.get('/smartspaces/spacecontroller/all.json').success(
            function(data) {
              $scope.spaceControllers = data.data;
            });
      } ]);

  module.controller('SpaceControllerDetailCtrl', [
      '$scope',
      '$http',
      '$routeParams',
      function($scope, $http, $routeParams) {
        $http.get(
            '/smartspaces/spacecontroller/'
                + $routeParams.spaceControllerId + '/view.json').success(
            function(data) {
              $scope.spaceControllerInfo = data.data;
            });
      } ]);

})();