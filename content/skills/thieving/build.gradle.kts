plugins {
    id("base-conventions")
}

dependencies {
    implementation(projects.api.dropTable)
    implementation(projects.api.dropTablePlugin)
    implementation(projects.api.invtx)
    implementation(projects.api.player)
    implementation(projects.api.pluginCommons)
    implementation(projects.api.random)
    implementation(projects.api.script)
    implementation(projects.content.skills.utils)
}
