environments {
    local {
        artifactory {
            host.url = 'http://localhost/artifactory'
            username = 'sample'
            password = 'sample'
        }
    }
    local {
        artifactory {
            host.url = 'http://dev/artifactory'
            username = 'sample'
            password = 'sample'
        }
    }
    live {
        artifactory {
            host.url = 'http://live/artifactory'
            username = 'sample'
            password = 'sample'
        }
    }
}