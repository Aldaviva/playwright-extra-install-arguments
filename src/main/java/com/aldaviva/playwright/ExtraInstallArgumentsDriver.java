package com.aldaviva.playwright;

import com.microsoft.playwright.Playwright.CreateOptions;
import com.microsoft.playwright.impl.PlaywrightImpl;
import com.microsoft.playwright.impl.driver.Driver;
import com.microsoft.playwright.impl.driver.jar.DriverJar;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>Alternative driver for <a href="https://playwright.dev/java/docs/intro">Playwright</a> that lets you specify custom arguments to the {@code install} command which downloads web browsers.</p>
 * <p>This is useful if you only want to spend the time and disk space to install one browser, instead of all of them, or if you want to include or exclude the Chromium headless shell.</p>
 * <p>This is easier than separately executing {@code mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install"} because you don't need to install Maven (a development tool) on your production deployment machines, or worry about forking processes.</p>

 * <h2>Usage</h2>
 * <ol><li>Specify the extra arguments you want to pass to {@code node cli.js install} by doing one of the following. Multiple arguments are delimited by spaces.
 * <ul><li>{@code CreateOptions createOptions = ExtraInstallArgumentsDriver.setExtraInstallArguments("chromium --with-deps --no-shell");}</li>
 * <li><code>CreateOptions createOptions = new CreateOptions().setEnv(new HashMap&lt;&gt;());
 * createOptions.env.put(ExtraInstallArgumentsDriver.EXTRA_INSTALL_ARGUMENTS, "chromium --with-deps --no-shell");</code></li></ul></li>
 * 
 * <li>Pass the {@link CreateOptions} to {@link PlaywrightImpl#create(CreateOptions)}.
 * <pre><code>try (Playwright playwright = PlaywrightImpl.create(createOptions)) {
 *    Browser chromium = playwright.chromium().launch();
 *}</code></pre></li></ol>
 *    
 * When the Playwright instance is created, it will automatically download Chromium and its dependencies, but not Firefox or WebKit.
 * 
 * <h3>Example</h3>
 * <pre>
 * <code>CreateOptions createOptions = ExtraInstallArgumentsDriver.setExtraInstallArguments("chromium --with-deps --no-shell");
 *
 * try (Playwright playwright = PlaywrightImpl.create(createOptions)) {
 *     Browser chromium = playwright.chromium().launch();
 * }</code></pre>
 */
public class ExtraInstallArgumentsDriver extends DriverJar {

	/**
	 * <p>If you want to pass extra command-line arguments to Playwright's {@code cli.js install}, add them as a space-delimited string to the {@link CreateOptions} environment map with this as the key.</p>
	 * <p>Alternatively, you may call {@link #setExtraInstallArguments(String)}.</p>
	 */
	public static final String EXTRA_INSTALL_ARGUMENTS = "PLAYWRIGHT_EXTRA_INSTALL_ARGUMENTS";
	private static final String DRIVER_IMPL_KEY = "playwright.driver.impl";
	private static final String DRIVER_IMPL_VALUE = ExtraInstallArgumentsDriver.class.getName();

	private static String oldDriverImpl = null;

	/**
	 * <p>Required no-arg constructor for {@link DriverJar} which will be called by {@link Driver#newInstance()}.</p>
	 * <p>Users shouldn't need to construct this directly; instead, just call {@link PlaywrightImpl#create(CreateOptions)} with the return value from {@link #setExtraInstallArguments(String)}.</p>
	 * @throws IOException thrown by {@link DriverJar#DriverJar()}
	 */
	public ExtraInstallArgumentsDriver() throws IOException {
		super();
	}

	/**
	 * <p>Register this class as the driver implementation that Playwright will use, instead of the default {@link DriverJar} implementation.</p>
	 * <p>This will also be called automatically when you call {@link #setExtraInstallArguments(String)}.</p>
	 */
	public static void activate() {
		final String oldValue = System.setProperty(DRIVER_IMPL_KEY, DRIVER_IMPL_VALUE);
		if (!DRIVER_IMPL_VALUE.equals(oldValue)) {
			oldDriverImpl = oldValue;
		}
	}

	/**
	 * Unregister this class so Playwright won't use it as the driver implementation for new {@link PlaywrightImpl#create(CreateOptions)} invocations, and they will revert to using the default {@link DriverJar} implementation.
	 */
	public static void deactivate() {
		if (oldDriverImpl != null) {
			System.setProperty(DRIVER_IMPL_KEY, oldDriverImpl);
		} else {
			System.clearProperty(DRIVER_IMPL_KEY);
		}
	}

	/**
	 * <p>Specify extra command-line arguments that should be passed to Playwright's {@code node cli.js install} invocation.</p>
	 * @param args Zero or more space-delimited command-line arguments (such as browser names) that will be passed to {@code cli.js} immediately after the {@code install} argument, such as {@code chromium}. See <a href="https://playwright.dev/docs/browsers">Playwright &rsaquo; Docs &rsaquo; Guides &rsaquo; Browsers</a> for available arguments.
	 * @return A {@link CreateOptions} instance that can be passed to {@link PlaywrightImpl#create(CreateOptions)}.
	 */
	public static CreateOptions setExtraInstallArguments(final String args) {
		return setExtraInstallArguments(null, args);
	}

	/**
	 * <p>Specify extra command-line arguments that should be passed to Playwright's {@code node cli.js install} invocation.</p>
	 * @param createOptions Environment variable holder that gets passed to {@link PlaywrightImpl#create(CreateOptions)}, or {@code null} to create a new empty instance
	 * @param args Zero or more space-delimited command-line arguments (such as browser names) that will be passed to {@code cli.js} immediately after the {@code install} argument, such as {@code chromium}. See <a href="https://playwright.dev/docs/browsers">Playwright &rsaquo; Docs &rsaquo; Guides &rsaquo; Browsers</a> for available arguments.
	 * @return A {@link CreateOptions} instance that can be passed to {@link PlaywrightImpl#create(CreateOptions)}.
	 */
	public static CreateOptions setExtraInstallArguments(CreateOptions createOptions, final String args) {
		activate();

		if (createOptions == null) {
			createOptions = new CreateOptions();
		}

		Map<String, String> env = createOptions.env;
		if (env == null) {
			env = new HashMap<>();
			createOptions.setEnv(env);
		}

		if (args != null) {
			env.put(EXTRA_INSTALL_ARGUMENTS, args);
		} else {
			env.remove(EXTRA_INSTALL_ARGUMENTS);
		}

		return createOptions;
	}

	@Override
	public ProcessBuilder createProcessBuilder() {
		final ProcessBuilder processBuilder = super.createProcessBuilder();
		final List<String> oldCommands = processBuilder.command();
		final String extraArgs = env.get(EXTRA_INSTALL_ARGUMENTS);
		final List<String> extraArgList = extraArgs != null ? Arrays.asList(extraArgs.split(" ")) : null;
		processBuilder.environment().remove(EXTRA_INSTALL_ARGUMENTS);
		return processBuilder.command(new ExtraInstallArgumentsList(oldCommands, extraArgList));
	}

}
