config.module.rules.push({
    test: /\.css$/,
    use: ['style-loader', 'css-loader'],
});

config.resolve.extensions = ['.js', '.css',]
