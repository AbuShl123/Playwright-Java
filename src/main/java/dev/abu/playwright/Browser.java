package dev.abu.playwright;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static dev.abu.utils.EnvParams.*;
import static dev.abu.utils.Logger.log;
import static dev.abu.utils.Status.FAIL;

public class Browser {

    public static final double TIMEOUT = 60_000;

    private static final Map<Playwright, com.microsoft.playwright.Browser> playwrightMap = new ConcurrentHashMap<>();
    private static final ThreadLocal<Playwright> playwright = new ThreadLocal<>();
    private static final ThreadLocal<com.microsoft.playwright.Browser> browser = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> context = new ThreadLocal<>();
    private static final ThreadLocal<Page> page = new ThreadLocal<>();
    private static final ThreadLocal<List<Page>> tabs = new ThreadLocal<>();

    private static void init() {
        log("Creating Playwright & Browser objects");

        var playwrightObj = Playwright.create();
        var browserObj = playwrightObj.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(HEADLESS)
                .setChannel("chrome")
                .setArgs(List.of("--start-maximized")));

        playwright.set(playwrightObj);
        browser.set(browserObj);
        playwrightMap.put(playwrightObj, browserObj);

        log("Initialized Playwright & Browser objects");
    }

    public static void initContext() {
        if (playwright.get() == null || browser.get() == null) init();

        BrowserContext ctx = browser.get().newContext(options());
        ctx.setDefaultTimeout(TIMEOUT);
        context.set(ctx);
        log("Started browser context");

        Page tab = ctx.newPage();
        tab.evaluate("document.body.style.zoom='0.75'");
        setCurrentPage(tab);

        tabs.set(new LinkedList<>());
        getTabs().add(tab);
    }

    public static void close() {
        log("Closing " + playwrightMap.size() + " Playwright & Browser objects");
        playwrightMap.forEach((playwright, browser) -> {
            if (browser != null) browser.close();
            if (playwright != null) playwright.close();
        });
        playwrightMap.clear();
    }

    public static void closeContext() {
        if (getContext() == null) return;
        log("Closing browser context");
        getContext().close();
        context.remove();
        page.remove();
        tabs.remove();
    }

    public static void navigate(String url) {
        try {
            getPage().navigate(url);
        } catch (Throwable e) {
            log(FAIL, "Couldn't open the url: " + url);
            throw e;
        }
    }

    public static void openNewTab(String url) {
        Page tab = getContext().newPage();
        tab.navigate(url);
        log("Opened a new tab with the following url: " + url);
        getTabs().add(tab);
        setCurrentPage(tab);
    }

    public static void openNewTab(Runnable callback) {
        try {
            Page tab = getContext().waitForPage(callback);
            log("Opened a new tab with the following url: " + tab.url());
            getTabs().add(tab);
            setCurrentPage(tab);
        } catch (Throwable e) {
            log(FAIL, "New tab didn't open");
            throw e;
        }
    }

    public static String tryOpenNewTab(Runnable callback) {
        Page tab = null;
        try {
            tab = getContext().waitForPage(new BrowserContext.WaitForPageOptions().setTimeout(10_000), callback);
        } catch (Throwable ignored) {
        }
        if (tab == null) return null;

        tab.waitForLoadState();
        log("Opened a new tab with the following url: " + tab.url());
        getTabs().add(tab);
        setCurrentPage(tab);
        return tab.url();
    }

    public static void gotoTab(int number) {
        Page tab = getTabs().get(number - 1);
        if (tab != null) {
            tab.bringToFront();
            setCurrentPage(tab);
        }
    }

    public static void gotoFirstTab() {
        gotoTab(1);
    }

    public static void gotoSecondTab() {
        gotoTab(2);
    }

    public static void gotoLastTab() {
        gotoTab(getTabs().size() - 1);
    }

    public static void closeAndGotoTab(int number) {
        closeCurrentTab();
        gotoTab(number);
    }

    public static void closeCurrentTab() {
        var page = getPage();
        if (page != null && !page.isClosed()) {
            getTabs().remove(page);
            page.close();
        }
    }

    public static void closeFirstTab() {
        Page tab = getTabs().remove(0);
        if (tab != null && !tab.isClosed()) {
            tab.close();
        }
    }

    public static void goBack() {
        getPage().goBack();
    }

    public static Page getPage() {
        return page.get();
    }

    public static Playwright getPlaywright() {
        return playwright.get();
    }

    private static void setCurrentPage(Page tab) {
        page.set(tab);
    }

    public static BrowserContext getContext() {
        return context.get();
    }

    public static List<Page> getTabs() {
        return tabs.get();
    }

    private static com.microsoft.playwright.Browser.NewContextOptions options() {
        var options = new com.microsoft.playwright.Browser.NewContextOptions()
                .setHttpCredentials(CRM_USERNAME, CRM_PASSWORD)
                .setViewportSize(null)
                .setStorageStatePath(Paths.get("outlook-session.json"));

        if (IS_PIPELINE || HEADLESS) {
            options.setViewportSize(1920, 1080);
        }

        if (MOBILE) {
            // MANY THINGS HERE! SHOULD BE COPY-PASTED
        }

        return options;
    }
}