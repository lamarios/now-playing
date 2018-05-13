var webpack = require('webpack');
var path = require('path');
var CopyWebpackPlugin = require('copy-webpack-plugin');


var BUILD_DIR = path.resolve(__dirname, '../resources/web/public');
var APP_DIR = path.resolve(__dirname, 'src');


var API_ROOT = "";
var API_URL = API_ROOT + '/api';

var constants = {
    ACTIVITIES: {
        GET_CURRENT_PLUGIN: JSON.stringify(API_URL + "/activities/get-current-plugin"),
        GET_AVAILABLE_PLUGINS: JSON.stringify(API_URL + "/activities/get-available-plugins"),
        SET_ACTIVITY_PLUGIN:JSON.stringify(API_URL + "/activities/set"),
        GET_MAPPING:JSON.stringify(API_URL + "/activities/get-mapping"),
        SET_MAPPING:JSON.stringify(API_URL + "/activities/set-mapping"),
        GET_ACTIVITIES:JSON.stringify(API_URL+"/activities/get-activities")
    },
    NOW_PLAYING:{
        GET_AVAILABLE_PLUGINS: JSON.stringify(API_URL + "/now-playing/get-available-plugins"),
        GET_IMAGE: JSON.stringify("/now-playing.jpg?width={0}&height={1}&scale={2}")
    },
    SETTINGS:{
        SAVE: JSON.stringify(API_URL + "/settings/save"),
    }
};

var config = {
    entry: [APP_DIR + '/jsx/index.jsx', APP_DIR + '/less/main.less'],
    output: {
        path: BUILD_DIR,
        filename: 'bundle.js'
    },
    plugins: [
        new webpack.DefinePlugin({
            'API': constants
        }),

        new CopyWebpackPlugin([
            {from: APP_DIR + '/index.html'}
        ], {
            copyUnmodified: true
        })
    ],
    module: {
        rules: [
            {
                test: /\.(woff|woff2|eot|ttf|svg)$/,
                loader: 'url-loader?limit=100000'
            },
            {
                test: /\.(jpg|png|svg|gif)$/,
                loader: 'file-loader?name=images/[name].[ext]',
                include: APP_DIR + '/images',
            },
            {
                test: /\.jsx?/,
                include: APP_DIR,
                loader: 'babel-loader',
                query: {
                    presets: ['es2015', 'react']
                }
            },
            {test: /\.css$/, loader: 'style-loader!css-loader',},
            {
                test: /main.less$/,
                use: [{
                    loader: "style-loader" // creates style nodes from JS strings
                }, {
                    loader: "css-loader" // translates CSS into CommonJS
                }, {
                    loader: "less-loader"
                }]
            }
        ]
    }
};

module.exports = config;