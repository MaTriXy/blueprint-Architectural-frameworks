@file:Suppress("unused")

package reactivecircus.blueprint

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.TestedExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.kotlin.dsl.getByType

/**
 * A plugin that provides baseline gradle configurations for all projects, including:
 * - root project
 * - Android Application projects
 * - Android Library projects
 * - Kotlin JVM projects
 * - Java JVM projects
 *
 * Apply this plugin to the build.gradle.kts file in all projects:
 * ```
 * plugins {
 *     id 'blueprint-plugin'
 * }
 * ```
 */
class BlueprintPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.afterEvaluate {
            configureForAllProjects()

            // apply configurations specific to root project
            if (isRoot) {
                configureRootProject()
            }

            // apply baseline configurations based on plugins applied
            plugins.all {
                when (this) {
                    is JavaPlugin,
                    is JavaLibraryPlugin -> {
                        project.convention.getPlugin(JavaPluginConvention::class.java).apply {
                            sourceCompatibility = JavaVersion.VERSION_1_8
                            targetCompatibility = JavaVersion.VERSION_1_8
                        }
                    }
                    is LibraryPlugin -> {
                        extensions.getByType<TestedExtension>().configureCommonAndroidOptions()
                        extensions.getByType<LibraryExtension>().configureAndroidLibraryOptions(project)
                    }
                    is AppPlugin -> {
                        extensions.getByType<TestedExtension>().configureCommonAndroidOptions()
                    }
                }
            }
        }
    }
}

val Project.isRoot get() = this == this.rootProject
