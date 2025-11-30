package com.foleyservices.tests;

import com.microsoft.playwright.*;
import com.microsoft.playwright.assertions.LocatorAssertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static com.foleyservices.playwright.Browser.TIMEOUT;
import static com.foleyservices.utils.EnvParams.*;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class Testing {

    public static final String URL = "https://practicetestautomation.com/practice-test-login/";
    private static final Logger log = LoggerFactory.getLogger(Testing.class);

    @Test
    public void test() {
        log.info("Launched playwright and browser");
        new Thread(Testing::runTest).start();
        new Thread(Testing::runTest).start();
    }

    private static void runTest() {
        try (
                var playwright = Playwright.create();
                var browser = playwright.chromium().launch(
                        new BrowserType.LaunchOptions()
                                .setHeadless(HEADLESS)
                                .setChannel("chrome")
                                .setArgs(List.of("--start-maximized"))
                                .setEnv(Map.of("DEBUG", "pw:api"))
                );
        ) {

            BrowserContext ctx1 = browser.newContext(options());
            ctx1.setDefaultTimeout(TIMEOUT);
            log.info("Launched browser context");
            Page page1 = ctx1.newPage();
            page1.navigate(URL);
            log.info("Navigated to URL");
            page1.locator("input#username").fill("student");
            page1.locator("input#password").fill("Password123");
            page1.locator("button#submit").click();
            assertThat(page1.locator("//strong")).hasText("Congratulations student. You successfully logged in!", new LocatorAssertions.HasTextOptions().setTimeout(TIMEOUT));
            log.info("Successfully logged in");
        } catch (Throwable e) {
            log.error("Error occurred in the test", e);
        }
    }

    private static synchronized Browser.NewContextOptions options() {
        var options = new Browser.NewContextOptions()
                .setHttpCredentials(CRM_USERNAME, CRM_PASSWORD)
                .setViewportSize(null);

        if (IS_PIPELINE || HEADLESS) {
            options.setViewportSize(1920, 1080);
        }

        if (MOBILE) {
            // MANY THINGS HERE! SHOULD BE COPY-PASTED
        }

        return options;
    }
}
