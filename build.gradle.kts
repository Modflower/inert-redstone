import java.net.URLEncoder
import java.nio.charset.StandardCharsets

plugins {
	alias(libs.plugins.loom)
	alias(libs.plugins.minotaur)
}

val projectVersion: String by project
val modrinthId: String by project
val githubRepository: String by project

val isPublish = System.getenv("GITHUB_EVENT_NAME") == "release"
val isRelease = System.getenv("BUILD_RELEASE").toBoolean()
val isActions = System.getenv("GITHUB_ACTIONS").toBoolean()
val baseVersion = "$projectVersion+mc.${libs.versions.minecraft.required.get()}"
version = when {
	isRelease -> baseVersion
	isActions -> "$baseVersion-build.${System.getenv("GITHUB_RUN_NUMBER")}-commit.${System.getenv("GITHUB_SHA").substring(0, 7)}-branch.${System.getenv("GITHUB_REF")?.substring(11)?.replace('/', '.') ?: "unknown"}"
	else -> "$baseVersion-build.local"
}

java {
	sourceCompatibility = JavaVersion.VERSION_1_8
	targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
	minecraft(libs.minecraft)
	mappings(variantOf(libs.fabric.yarn) { classifier("v2") })

	modImplementation(libs.fabric.loader)
	modImplementation(libs.bundles.mod.dependency)
}

tasks {
	processResources {
		inputs.property("version", version)

		filesMatching("fabric.mod.json") {
			expand(
				"java" to java.targetCompatibility.majorVersion,
				"version" to version,
				"minecraftRequired" to libs.versions.minecraft.required.get(),
				"githubRepository" to githubRepository,
			)
		}
	}

	withType<JavaCompile> {
		options.encoding = "UTF-8"
	}

	jar {
		from("LICENSE") {
			rename { "${it}_${archiveBaseName.get()}" }
		}
	}
}

modrinth {
	token.set(System.getenv("MODRINTH_TOKEN"))
	projectId.set(modrinthId)
	versionType.set(
		System.getenv("RELEASE_OVERRIDE") ?: when {
			"alpha" in projectVersion -> "alpha"
			!isRelease || '-' in projectVersion -> "beta"
			else -> "release"
		}
	)
	val ref = System.getenv("GITHUB_REF")
	changelog.set(
		System.getenv("CHANGELOG") ?: if (ref != null && ref.startsWith("refs/tags/")) "You may view the changelog at https://github.com/$githubRepository/releases/tag/${urlEncode(ref.substring(10))}"
		else "No changelog is available. Perhaps poke at https://github.com/$githubRepository for a changelog?"
	)
	uploadFile.set(tasks.remapJar.get())
	gameVersions.set(libs.versions.minecraft.compatible.get().split(","))
	loaders.addAll("fabric", "quilt")
}

@Suppress("Since15") // Irrelevant as Loom requires Java 17 anyways.
fun urlEncode(str: String) = URLEncoder.encode(str, StandardCharsets.UTF_8)
