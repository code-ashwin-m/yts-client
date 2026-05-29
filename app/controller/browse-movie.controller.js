angular.module("YTS").controller("BrowseMovieController", function ($scope, $location, ApiService) {
    $scope.movies = [];

    $scope.loadMovies = function() {
        ApiService.list()
        .then(function (response) {
            $scope.movies = response.data.movies;
        })
        .catch(() => {});
    };

    $scope.loadMovies();

});