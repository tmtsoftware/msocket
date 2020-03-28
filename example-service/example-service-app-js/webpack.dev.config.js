var path = require('path');
var HtmlWebpackPlugin = require('html-webpack-plugin');

module.exports = {
    module: {
        rules: [
            {
                test: /\.css$/,
                use: ['style-loader', 'css-loader']
            },
            {
                test: /\.(ttf|eot|woff|png|glb)$/,
                use: 'file-loader'
            },
            {
                test: /\.(eot)$/,
                use: 'url-loader'
            }
        ]
    },
    entry: [
        path.resolve(__dirname, './target/scala-2.13/example-service-app-js-fastopt.js'),
    ],
    plugins: [
        new HtmlWebpackPlugin({
            filename: 'index.html',
            template: './html/index.html'
        })
    ]
}
