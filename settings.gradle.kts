pluginManagement {
  repositories {
    google {
      content {
        includeGroupByRegex("com.android.*")
        includeGroupByRegex("com.google.*")
        includeGroupByRegex("androidx..*")
      }
    }
    mavenCentral()
    gradlePluginPortal()
  }
  resolutionStrategy {
    eachPlugin {
      if (requested.id.id == "com.google.android.gms.oss-licenses-plugin") {
        useModule("com.google.android.gms:oss-licenses-plugin:0.10.6")
      }
    }
  }
}

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    //        mavenLocal()
    google()
    mavenCentral()
  }
}

rootProject.name = "AI Edge Gallery"

include(":app")
