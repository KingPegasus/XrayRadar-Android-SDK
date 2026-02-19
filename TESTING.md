# Testing

## Android SDK location

Gradle must know where the Android SDK is. Use either:

- **Environment variable:** set `ANDROID_HOME` (or `ANDROID_SDK_ROOT`) to your SDK path, e.g.  
  `export ANDROID_HOME=$HOME/Android/Sdk`
- **Project file:** create `local.properties` in the project root (this file is gitignored) with:
  ```properties
  sdk.dir=/path/to/your/Android/Sdk
  ```
  Example if the SDK is in the default location:  
  `sdk.dir=/home/raza/Android/Sdk`

## Installing the Android SDK (Ubuntu)

If you don’t have the SDK yet, you can install the command-line tools via apt. `sdkmanager` is not a standalone package; install one of the command-line tool versions:

```bash
sudo apt install google-android-cmdline-tools-13.0-installer
```

The SDK is often installed under `/usr/lib/android-sdk`. Set the project to use it:

```bash
echo 'sdk.dir=/usr/lib/android-sdk' > local.properties
```

Then install the components needed to build and run tests. This project uses **compileSdk 35**, so install matching platform and build-tools. The system SDK at `/usr/lib/android-sdk` is not writable by normal users, so use `sudo`:

```bash
export ANDROID_HOME=/usr/lib/android-sdk
sudo $ANDROID_HOME/cmdline-tools/13.0/bin/sdkmanager "platform-tools" "platforms;android-35" "build-tools;35.0.0"
```

You will be prompted to accept the Android SDK license; type `y` and Enter. If you use a user-writable SDK path (e.g. `$HOME/Android/Sdk` from Android Studio), you can run `sdkmanager` without `sudo` and Gradle will be able to install missing components automatically.

If your SDK is in a different path (e.g. `$HOME/Android/Sdk` from Android Studio), use that for `ANDROID_HOME` and in `local.properties` instead.

## Optional: Conda environment for JDK

You can use conda to provide a pinned JDK 17 for running tests (no Python required for the Android SDK):

```bash
conda create -n android openjdk=17 -c conda-forge
conda activate android
export JAVA_HOME=$CONDA_PREFIX
```

Then run the commands below in the same shell. The Android SDK and Gradle are unchanged; only the JDK is supplied by conda.

## Gradle wrapper

Use the project wrapper (`./gradlew`) from the repo root, not the system `gradle` command, so the correct project and settings are used.

If you see **Missing gradle/wrapper/gradle-wrapper.jar**, generate the wrapper (requires a system Gradle or JDK):

```bash
gradle wrapper --gradle-version 8.10.2
```

Then run the test/coverage commands below. Commit `gradle/wrapper/gradle-wrapper.jar` so others and CI don’t need to do this.

## Run unit tests

From the project root:

```bash
./gradlew :library:testDebugUnitTest
```

## Generate coverage report

```bash
./gradlew :library:jacocoTestReport
```

Coverage output:

- HTML: `library/build/reports/jacoco/jacocoTestReport/html/index.html`
- XML: `library/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml`
