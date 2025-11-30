package com.foleyservices.framework;

import com.foleyservices.playwright.Browser;
import org.slf4j.MDC;
import org.testng.ITestContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;

import java.lang.reflect.Method;

import static com.foleyservices.utils.Logger.log;

public class TestBase {

    @BeforeMethod
    public void beforeMethod(ITestContext ctx, Method method) {
        log("************* Before Method - " + method.getName() + " *************");
        MDC.put("testName", method.getName());
        Browser.initContext();
    }

    @AfterMethod
    public void afterMethod() {
        MDC.remove("testName");
        Browser.closeContext();
    }

    @AfterSuite
    public void afterSuite() {
        log("************* After Suite *************");
        Browser.close();
    }
}
