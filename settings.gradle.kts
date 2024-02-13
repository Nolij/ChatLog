pluginManagement {
	repositories {
		maven("https://maven.fabricmc.net/")
		gradlePluginPortal()
	}
}

plugins {
	id("fabric-loom") version("1.5.+") apply(false)
}