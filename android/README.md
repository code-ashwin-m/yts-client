# YTS Client Android

Android implementation of the existing web movie browser.

## Structure

- `app/src/main/java/com/example/ytsclient/MainActivity.kt` contains the Jetpack Compose UI.
- `app/src/main/java/com/example/ytsclient/ui/MovieViewModel.kt` owns screen state and calls the Java backend layer.
- `app/src/main/java/com/example/ytsclient/data/` contains the Java API, settings, SQLite bookmark, image cache, and model logic.

## Features

- Browse movies from the same YTS-style API used by the web app.
- Configure the API base URL from the Settings tab.
- Browse in grid or list mode.
- Toggle list/grid mode and adjust grid columns from the Browse tab.
- Configure page size from Settings.
- Hide or show the search/filter panel from the Browse tab.
- Search/filter controls automatically hide when scrolling down.
- Swipe left to Browse and swipe right to Favorites.
- Open Settings from the top-right overflow menu.
- Load more movies automatically when scrolling near the bottom.
- Bookmark movies from the detail screen.
- Favorites tab stores only movie IDs in SQLite and fetches current movie data from the server when opened.
- Image URLs are hashed with SHA-256 and cached for 7 days under the app-specific `Android/image-cache` folder.
- JSON responses are parsed with Gson into Java model classes.

## Build

Open the `android` folder in Android Studio and run the `app` configuration, or run:

```bash
./gradlew assembleDebug
```
