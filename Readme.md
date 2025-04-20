🎭 playwright-extra-install-arguments
===

[![Maven Central Version](https://img.shields.io/maven-central/v/com.aldaviva/playwright-extra-install-arguments?logo=apachemaven&label=maven&color=success)](https://central.sonatype.com/artifact/com.aldaviva/playwright-extra-install-arguments)

*Pass extra custom arguments to Playwright's `cli.js` install command, which lets you install one specific browser instead of all supported browsers.*

[Playwright](https://playwright.dev) is a library that lets you programmatically control web browsers, which is useful for automated testing, archiving, and scraping. It supports Chromium (including Chrome and Edge), Firefox, and WebKit. You can use Playwright in Node.js, Java, .NET, and Python.

<!-- MarkdownTOC autolink="true" bracket="round" autoanchor="false" levels="1,2,3" bullets="1.,-" -->

1. [Problem](#problem)
1. [Solution](#solution)
1. [Prerequisites](#prerequisites)
1. [Installation](#installation)
1. [Usage](#usage)

<!-- /MarkdownTOC -->

## Problem
Unfortunately, the [Java implementation of Playwright](https://playwright.dev/java/docs/intro) has a limitation in its API that prevents you from choosing which browser you want to download. Every time the Playwright is constructed, it will install the latest versions of all available browsers and their dependencies, which can take a lot of time, bandwidth, and disk space (**926 MB** on 2025-04-20). This is especially important in environments with constrained resources, like virtual machines, containers, Function-as-a-Service executions, or any machines with slow or expensive network connections, slow or small storage, or small transfer quotas.

The Node.js API does let you [specify exactly which browsers you want to download](https://playwright.dev/docs/browsers#install-browsers) by passing their names to the `install` command:
```sh
node cli.js install chromium
```

Unfortunately, despite bundling and calling the Node.js library internally, the Java API does not expose this functionality, and always insists on calling `node cli.js install` without any browser names. This leads to all browers always being installed.

This issue [has been raised](https://github.com/microsoft/playwright-java/issues/215) to the Playwright maintainers [multiple times](https://github.com/microsoft/playwright-java/issues/388), but they close it each time and offer an insufficient workaround:
1. install Maven (a development tool) on your production deployment machine
1. manually fork a new process `mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install chromium"`, along with all of the associated process management boilerplate and pitfalls
1. call `Driver.ensureDriverInstalled(env, false)` before calling `PlaywrightImpl.create(CreateOptions)`

## Solution
This library offers an alternative which can easily install only your desired browsers, without forking any processes or manual installations.

It is a subclass of Playwright's default `Driver` which lets you specify extra arguments to append to the `cli.js install` command using an environment variable.

## Prerequisites
- [Java 8 runtime or later](https://adoptium.net/temurin/releases/)
- [Playwright for Java](https://central.sonatype.com/artifact/com.microsoft.playwright/playwright) 1.51 or later

## Installation
Add a dependency on `com.aldaviva:playwright-extra-install-arguments` to your Maven-compatible dependency management system. It's published in the Maven Central repository.

```xml
<dependency>
    <groupId>com.aldaviva</groupId>
    <artifactId>playwright-extra-install-arguments</artifactId>
    <version><!-- whatever the latest version is--></version>
</dependency>
```

## Usage

See [the Playwright Node.js Browser documentation](https://playwright.dev/docs/browsers) for all of the arguments you can pass to the `install` command, such as browser names, `--with-deps`, and `--no-shell`/`--only-shell`.

The following example only installs the headless Chromium shell (and its dependencies like ffmpeg), skipping full Chromium, Firefox, and WebKit and saving you 729 MB of downloads and disk space (79%).

```java
import com.aldaviva.playwright.ExtraInstallArgumentsDriver;

public class Main {

    public static void main(String[] args) {
        ExtraInstallArgumentsDriver.activate(); // register this Driver class
        CreateOptions createOptions = ExtraInstallArgumentsDriver.setExtraInstallArguments("chromium --with-deps --only-shell");

        try (Playwright playwright = PlaywrightImpl.create(createOptions)) {
            Browser chromium = playwright.chromium().launch(new LaunchOptions().setHeadless(true));
            BrowserContext browserContext = chromium.newContext();

            try (Page page = browserContext.newPage()) {
                page.navigate("https://www.aldaviva.com/");
                System.out.println(page.title());
            }
        }
    }

}
```

- `ExtraInstallArgumentsDriver.activate()` must be called once before any calls to `PlaywrightImpl.create(CreateOptions)`, so that Playwright will use `ExtraInstallArgumentsDriver` instead of the default `DriverJar`.
- Multiple extra arguments can be separated by a space, comma, or vertical pipe.
- If you already have an existing `CreateOptions` instance you want to continue using, you may also manually set the extra arguments string as the `PLAYWRIGHT_EXTRA_INSTALL_ARGUMENTS` environment variable (whose name is exposed as the `ExtraInstallArgumentsDriver.EXTRA_INSTALL_ARGUMENTS` constant).
    ```java
    CreateOptions createOptions = new CreateOptions().setEnv(new HashMap<>());
    createOptions.env.put(ExtraInstallArgumentsDriver.EXTRA_INSTALL_ARGUMENTS, "firefox");
    
    try (Playwright playwright = PlaywrightImpl.create(createOptions)) {
    ```