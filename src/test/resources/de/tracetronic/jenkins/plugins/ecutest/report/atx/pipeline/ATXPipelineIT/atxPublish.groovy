node {
    def server = ATX.server('test.guide')
    server.publish() // will fail due to missing ecu.test installation
}
