angular.module("YTS").service("ApiService", function($http, $q) {
    const base = "https://movies-api.accel.li/api/v2";

    function handleResponse(response) {
        // API expected to return { items, total } for list endpoints,
        // or the created/updated object for create/update
        return response.data;
    };
    
    function handleError(err) {
        console.error("API error:", err);
        return $q.reject(err);
    };

    this.list = function (){
        return $http.get(`${base}/list_movies.json?quality=3D`)
        .then(handleResponse)
        .catch(handleError);
    };

    this.movie = function (id) {
        const params = {
            movie_id: id,
            with_images: true
        };
        return $http.get(`${base}/movie_details.json`, { params: params })
        .then(handleResponse)
        .catch(handleError);
    }
});