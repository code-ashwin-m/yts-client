
alert("hai");
angular.module("YTS").config(function ($routeProvider) {
    $routeProvider
    .when("/", {
        templateUrl: "app/ui/main.html"
    });
});