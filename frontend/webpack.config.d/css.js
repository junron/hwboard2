config.module.rules.push({
    test: /\.css$/,
    use: ['style-loader', 'css-loader'],
});

config.module.rules.push({
    test: /\.(woff(2)?|ttf|eot|svg)(\?v=\d+\.\d+\.\d+)?$/,
    use: [{
        loader: 'file-loader',
        options: {
            name: '[name].[ext]',
            outputPath: 'fonts/'
        }
    }]
});

config.resolve.extensions = ['.js', '.css',];

config.node = {
    fs: 'empty'
};
