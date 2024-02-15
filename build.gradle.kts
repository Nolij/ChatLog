plugins {
	id("fabric-loom")
}

operator fun String.invoke(): String {
	return (rootProject.properties[this] as String?)!!
}

version = "mod_version"()
group = "maven_group"()

base {
	archivesName = "mod_id"()
}

repositories {
	mavenLocal()
	maven("https://maven.shedaniel.me/")
	maven("https://maven.terraformersmc.com/releases/")
	maven("https://api.modrinth.com/maven") {
		content {
			includeGroup("maven.modrinth")
		}
	}
}

dependencies {
	fun includeImplementation(dep: Any) {
		implementation(dep)
		include(dep)
	}
	
	fun includeRuntime(dep: Any) {
		modRuntimeOnly(dep)
		include(dep)
	}
	
	// Fabric
	minecraft("com.mojang:minecraft:${"minecraft_version"()}")
	mappings("net.fabricmc:yarn:${"minecraft_version"()}+build.${"mappings_version"()}:v2")
	modImplementation("net.fabricmc:fabric-loader:${"fabric_version"()}")
	modImplementation("net.fabricmc.fabric-api:fabric-api:${"fabric_api_version"()}")

	// Mod APIs
	modImplementation("me.shedaniel.cloth:cloth-config-fabric:${"cloth_config_version"()}")
	modImplementation("com.terraformersmc:modmenu:${"mod_menu_version"()}")

	// Java APIs
	includeImplementation("org.reflections:reflections:${"reflections_version"()}")
	include("org.javassist:javassist:${"javassist_version"()}")

	// Bundled Mods
	includeRuntime("maven.modrinth:no-chat-reports:${"no_chat_reports_version"()}")
	includeRuntime("maven.modrinth:moddetectionpreventer:${"mod_detection_preventer_version"()}")
	
	// Mods for dev environment
//	modRuntimeOnly("dev.nolij:zume:0.14.1-dev.2:modern")
	modRuntimeOnly("maven.modrinth:zume:0.14.0")
	modRuntimeOnly("maven.modrinth:modernfix:oJUG6agJ")
	modRuntimeOnly("maven.modrinth:embeddium:JVm1F3Ne")
	modRuntimeOnly("maven.modrinth:reeses-sodium-options:mc1.20.1-1.7.2")
	modRuntimeOnly("maven.modrinth:sodium-extra:mc1.20.1-0.5.4")
}

tasks.processResources {
	inputs.file("gradle.properties")

	filesMatching("fabric.mod.json") {
		expand(rootProject.properties)
	}
}

loom {
	accessWidenerPath = file("src/main/resources/chatlog.accesswidener")
}

tasks.withType<JavaCompile> {
	options.encoding = "UTF-8"
	options.release = 17
}

java {
	sourceCompatibility = JavaVersion.VERSION_17
	targetCompatibility = JavaVersion.VERSION_17
	withSourcesJar()
}

tasks.jar {
	from("LICENSE") {
		rename { "${it}_${"mod_id"()}" }
	}
}