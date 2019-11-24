if (defined.PRODUCTION === false || defined.PRODUCTION === 'false') {
    config.devServer = {
        watchOptions: {
            aggregateTimeout: 1000,
            poll: 100
        }
    }
}
