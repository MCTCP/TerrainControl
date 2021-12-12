plugins {
    base
}

tasks {
    clean {
        delete(layout.buildDirectory.dir("distributions"))
    }
}
