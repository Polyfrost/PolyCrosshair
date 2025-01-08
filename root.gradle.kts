plugins {
    id("dev.deftu.gradle.multiversion-root")
}

preprocess {
    // Adding new versions/loaders can be done like so:
    // For each version, we add a new wrapper around the last from highest to lowest.
    // Each mod loader needs to link up to the previous version's mod loader so that the mappings can be processed from the previous version.
    // "1.12.2-forge"(11202, "srg") {
    //     "1.8.9-forge"(10809, "srg")
    // }

    "1.12.2-fabric"(1_12_02, "yarn") {
        "1.12.2-forge"(1_12_02, "srg") {
            "1.8.9-forge"(1_08_09, "srg") {
                "1.8.9-fabric"(1_08_09, "yarn")
            }
        }
    }

    strictExtraMappings.set(true)
}
