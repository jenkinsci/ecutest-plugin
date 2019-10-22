node {
    def server = ATX.server('TEST-GUIDE')
    server.publish() // will fail due to missing ECU-TEST installation
}
