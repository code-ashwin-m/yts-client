angular.module("YTS").controller("MovieController", function ($scope, $location, $sce, ApiService) {
    $scope.movie = {};

    $scope.load = function(id){
        ApiService.movie(id)
        .then(function (response){
            $scope.movie = response.data.movie;
        });
    }

    $scope.magnetURL = function(){

        let magnet =
            "magnet:?xt=urn:btih:TORRENT_HASH" +
            "&dn=Movie+Name" +
            "&tr=udp://tracker.opentrackr.org:1337/announce";

        return $sce.trustAsUrl(magnet);
    };

    let id = $location.search().id || null;
    $scope.load(id);
});