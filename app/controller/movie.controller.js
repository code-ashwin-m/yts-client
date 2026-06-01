angular.module("YTS").controller("MovieController", function ($scope, $location, $sce, ApiService) {
    $scope.trackers = [
        "udp://glotorrents.pw:6969/announce",
        "udp://tracker.opentrackr.org:1337/announce",
        "udp://torrent.gresille.org:80/announce",
        "udp://tracker.openbittorrent.com:80",
        "udp://tracker.coppersurfer.tk:6969",
        "udp://tracker.leechers-paradise.org:6969",
        "udp://p4p.arenabg.ch:1337",
        "udp://tracker.internetwarriors.net:1337"
    ];

    $scope.movie = {};

    $scope.load = function(id){
        ApiService.movie(id)
        .then(function (response){
            $scope.movie = response.data.movie;
        });
    }

    $scope.magnetURL = function(torrent){
        let encodedTitle = encodeURIComponent($scope.movie.title_long + ` [${torrent.quality}]`);

        let magnet =
            `magnet:?xt=urn:btih:${torrent.hash}` +
            `&dn=${encodedTitle}`;

        $scope.trackers.forEach(function(tracker) {
            magnet += `&tr=${tracker}`;
        });

        return $sce.trustAsUrl(magnet);
    };

    $scope.torrentLabel = function(torrent){
        return `${torrent.quality}.${torrent.type.toUpperCase()}.${torrent.video_codec.toUpperCase()}`;
    }

    let id = $location.search().id || null;
    $scope.load(id);
});