config.module.rules.push({
    test: /\.html$/,
    use: [{
        loader: 'file-loader',
        options: {
            name: '[name].[ext]',
            outputPath: 'pages/'
        }
    }]
});
