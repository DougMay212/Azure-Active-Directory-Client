var gulp = require('gulp'),
    webserver = require('gulp-webserver'),
    del = require('del'),
    sass = require('gulp-sass'),
    jshint = require('gulp-jshint'),
    sourcemaps = require('gulp-sourcemaps'),
    browserify = require('browserify'),
    source = require('vinyl-source-stream'),
    buffer = require('vinyl-buffer'),
    uglify = require('gulp-uglify'),
    ngAnnotate = require('browserify-ngannotate'),
    CacheBuster = require('gulp-cachebust');

var cachebust = new CacheBuster();

gulp.task('clean', function(cb) {
    del(['./src/main/resources/static/dist'], cb);
});

gulp.task('build-css', ['clean'], function () {
    return gulp.src([
        './src/main/resources/static/content/scss/*',
        'node_modules/bootstrap/scss/bootstrap.scss'])
        .pipe(sourcemaps.init())
        .pipe(sass())
        .pipe(cachebust.resources())
        .pipe(sourcemaps.write('./css'))
        .pipe(gulp.dest('./src/main/resources/static/dist/css'));
});

gulp.task('img', ['clean'], function () {
    return gulp.src('./src/main/resources/static/content/img/*')
        .pipe(gulp.dest('./src/main/resources/static/dist/img'));
});

gulp.task('build-view-cache', ['clean'], function () {

    var ngHtml2Js = require("gulp-ng-html2js"),
        concat = require("gulp-concat");

    return gulp.src("./src/main/resources/static/views/*.html")
        .pipe(ngHtml2Js({
            moduleName: "views",
            prefix: "/views/"
        }))
        .pipe(concat("viewCache.js"))
        .pipe(gulp.dest("./src/main/resources/static/dist"));
});

gulp.task('jshint', function () {
    gulp.src('./src/main/resources/static/*.js')
        .pipe(jshint())
        .pipe(jshint.reporter('default'));
});

gulp.task('build-js', ['clean', 'build-view-cache'], function () {
    var b = browserify({
        entries: './src/main/resources/static/scripts/app.js',
        debug: true,
        paths: [
            './src/main/resources/static/scripts/controller',
            './src/main/resources/static/scripts/service',
            './src/main/resources/static/scripts',
            'node_modules/bootstrap/dist/js/bootstrap.min.js',
            'node_modules/jquery/dist/jquery.min.js',
            'node_modules/tether/dist/js/tether.min.js'],
        transform: [ngAnnotate]
    });

    return b.bundle()
        .pipe(source('bundle.js'))
        .pipe(buffer())
        .pipe(cachebust.resources())
        .pipe(sourcemaps.init({loadMaps: true}))
        .pipe(uglify())
        .pipe(sourcemaps.write('./'))
        .pipe(gulp.dest('./src/main/resources/static/dist/js/'));
});

gulp.task('build', ['clean', 'build-css', 'img', 'build-view-cache',
    'jshint', 'build-js'], function () {
    return gulp.src('./src/main/resources/static/index.html')
        .pipe(cachebust.references())
        .pipe(gulp.dest('./src/main/resources/static/dist'));
});

gulp.task('watch', function () {
    return gulp.watch([
        './src/main/resources/static/index.html',
        './src/main/resources/static/**/*.html',
        './src/main/resources/static/content/css/*.*css',
        './src/main/resources/static/**/*.js'], ['build']);
});

gulp.task('webserver', ['watch', 'build'], function () {
    gulp.src('.')
        .pipe(webserver({
            livereload: true,
            directoryListing: true,
            open: "http://localhost:8000/src/main/resources/static/dist/index.html",
            proxies: [{
                source: '/message',
                target: 'http://localhost:8080/message'
            }]
        }));
});

gulp.task('dev', ['watch', 'webserver']);

gulp.task('prepare-for-maven-jar', function () {
    return gulp.src('./src/main/resources/static/dist/**')
        .pipe(gulp.dest('target/classes/static'));
});

gulp.task('build-jar', ['build'], function () {
    gulp.start('prepare-for-maven-jar');
});