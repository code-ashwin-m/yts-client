angular.module("YTS").controller("BrowseMovieController", function ($scope, $location, ApiService) {
    $scope.movies = [];
    $scope.pageSize = $location.search().limit || 20;
    $scope.currentPage = $location.search().page || 1;
    $scope.totalPages = 0;
    $scope.totalMovies = 0;
    $scope.searchQuery = $location.search().query_term || "";
    $scope.rating = $location.search().minimum_rating || "all";
    $scope.ratings = [
        { value: "all", label: "All"},
        { value: "9", label: "9+"},
        { value: "8", label: "8+"},
        { value: "7", label: "7+"},
        { value: "6", label: "6+"},
        { value: "5", label: "5+"},
        { value: "4", label: "4+"},
        { value: "3", label: "3+"},
        { value: "2", label: "2+"},
        { value: "1", label: "1+"}
    ]
    $scope.quality = $location.search().quality || "all";
    $scope.qualities = [
        { value: "all", label: "All"},
        { value: "480p", label: "480p"},
        { value: "720p", label: "720p"},
        { value: "1080p", label: "1080p"},
        { value: "1080p.x265", label: "1080p.x265"},
        { value: "2160p", label: "2160p"},
        { value: "3D", label: "3D"}
    ]
    $scope.genre = $location.search().genre || "all";
    $scope.genres = [
        { value: "all", label: "All"},
        { value: "action", label: "Action"},
        { value: "adventure", label: "Adventure"},
        { value: "animation", label: "Animation"},
        { value: "biography", label: "Biography"},
        { value: "comedy", label: "Comedy"},
        { value: "crime", label: "Crime"},
        { value: "documentary", label: "Documentary"},
        { value: "drama", label: "Drama"},
        { value: "family", label: "Family"},
        { value: "fantasy", label: "Fantasy"},
        { value: "film-noir", label: "Film-Noir"},
        { value: "game-show", label: "Game-Show"},
        { value: "history", label: "History"},
        { value: "horror", label: "Horror"},
        { value: "music", label: "Music"},
        { value: "musical", label: "Musical"},
        { value: "mystery", label: "Mystery"},
        { value: "news", label: "News"},
        { value: "reality-tv", label: "Reality-TV"},
        { value: "romance", label: "Romance"},
        { value: "sci-fi", label: "Sci-Fi"},
        { value: "sport", label: "Sport"},
        { value: "talk-show", label: "Talk-Show"},
        { value: "thriller", label: "Thriller"},
        { value: "war", label: "War"},
        { value: "western", label: "Western"}
    ]

    function updatePagination(){
        if ($scope.totalMovies < 1) $scope.currentPage = 1;
        $scope.totalPages = Math.ceil($scope.totalMovies / $scope.pageSize) || 1;
    }

    $scope.nextPage = function () {
        if ($scope.currentPage < $scope.totalPages) {
            $scope.currentPage++;
            $scope.loadMovies();
        }
    }

    $scope.prevPage = function () {
        if ($scope.currentPage > 1) {
            $scope.currentPage--;
            $scope.loadMovies();
        }
    }

    $scope.firstPage = function () {
        $scope.currentPage = 1;
        $scope.loadMovies();
    }

    $scope.lastPage = function () {
        $scope.currentPage = $scope.totalPages;
        $scope.loadMovies();
    }

    $scope.search = function() {
        $scope.loadMovies();
    }

    $scope.loadMovies = function() {
        const params = {
            limit: $scope.pageSize,
            page: $scope.currentPage,
            query_term: $scope.searchQuery,
            quality: $scope.quality,
            minimum_rating: $scope.rating,
            genre: $scope.genre
        };

        $location.search(params);

        ApiService.list(params)
        .then(function (response) {
            $scope.movies = response.data.movies;
            $scope.totalMovies = response.data.movie_count;
            updatePagination();
        })
        .catch(() => {
            $scope.totalMovies = 0;
            updatePagination();
        });
    };

    $scope.loadMovies();

});