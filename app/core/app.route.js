angular.module("YTS").config(function ($routeProvider) {
    $routeProvider
    .when("/browse-movies", {
        templateUrl: "app/ui/browse-movies.html"
    })
    .when("/movie", {
        templateUrl: "app/ui/movie.html"
    })
    .when("/", {
        templateUrl: "app/ui/main.html"
    });
});