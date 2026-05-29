angular.module("YTS").controller("MovieController", function ($scope, $location, ApiService) {
    $scope.movie = {};

    $scope.load = function(id){
        ApiService.movie(id)
        .then(function (response){
            $scope.movie = response.data.movie;
        });
    }


    let id = $location.search().id || null;
    $scope.load(id);
});