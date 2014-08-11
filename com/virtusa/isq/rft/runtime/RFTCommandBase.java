/*
 * Copyright 2004 ThoughtWorks, Inc. Licensed under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.virtusa.isq.rft.runtime;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import com.ibm.gsk.ikeyman.error.NotImplementedException;
import com.rational.test.ft.object.interfaces.BrowserTabTestObject;
import com.rational.test.ft.object.interfaces.BrowserTestObject;
import com.rational.test.ft.object.interfaces.DocumentTestObject;
import com.rational.test.ft.object.interfaces.GuiTestObject;
import com.rational.test.ft.object.interfaces.ProcessTestObject;
import com.rational.test.ft.object.interfaces.RootTestObject;
import com.rational.test.ft.object.interfaces.SelectGuiSubitemTestObject;
import com.rational.test.ft.object.interfaces.StatelessGuiSubitemTestObject;
import com.rational.test.ft.object.interfaces.TestObject;
import com.rational.test.ft.object.interfaces.TextGuiTestObject;
import com.rational.test.ft.script.Property;
import com.rational.test.ft.script.RationalTestScript;
import com.rational.test.ft.script.impl.UserAbortedActionException;
import com.rational.test.ft.vp.ITestDataTable;
import com.virtusa.isq.rft.objectmap.ObjectLocator;
import com.virtusa.isq.rft.objectmap.ObjectMapParser;
import com.virtusa.isq.rft.utils.KeyCodes;
import com.virtusa.isq.rft.utils.PropertyHandler;
import com.virtusa.isq.rft.utils.Utils;

/**
 * The Class RFTCommandBase.
 */
@SuppressWarnings("static-access")
public class RFTCommandBase implements ICommandBase {

    /** The RETRYCOUNT. */
    private static final int RETRYCOUNT = 12;

    /** The RETRYINTEVAL. */
    private static final int RETRYINTEVAL = 1000;

    /** The retry. */
    private int retry = RETRYCOUNT;

    /** The retry interval. */
    private int retryInterval = RETRYINTEVAL;

    /** The object map. */
    private ObjectMapParser objectMap;

    /** The script. */
    private RationalTestScript script;

    /** The report logger. */
    private ReportLogger reportLogger;

    /** The application process ID. */
    private ProcessTestObject processTestObject;

    /**
     * Gets the report logger.
     * 
     * @return the report logger
     */
    public final ReportLogger getReportLogger() {
        return reportLogger;
    }

    /**
     * Instantiates a new RFT command base.
     * 
     * @param testScript
     *            the Test Script
     */
    public RFTCommandBase(final RationalTestScript testScript) {
        this.script = testScript;
        objectMap = new ObjectMapParser();
        reportLogger = new ReportLogger();
    }

    @Override
    public void open(String url, String waitTime) {

        open(url, "", waitTime);
    }

    @Override
    public void open(String url, String identifier, String waitTime) {

        String resolvedUrl = objectMap.getResolvedSearchPath(url, identifier);
        doOpen(resolvedUrl, waitTime);

    }

    /**
     * Override.
     * 
     * @param url
     *            the url
     * @param waitTime
     *            the wait time
     * @see com.virtusa.isq.rft.runtime.ICommandBase#open(java.lang.String,
     *      java.lang.String)
     */
    public final void doOpen(final String url, final String waitTime) {

        try {
            PropertyHandler propfile =
                    new PropertyHandler("RUNTIME.properties");
            String browser = propfile.getRuntimeProperty("BROWSER");
            final int time = 1000;
            processTestObject = script.startBrowser(browser, url);
            double wait = ((Double.parseDouble(waitTime)) / time);
            script.sleep(wait);
            startOfBrowserSession(browser);
            reportResults(ReportLogger.ReportLevel.SUCCESS, "Open", "Success",
                    "Open command passed. URL : " + url);
        } catch (Exception e) {
            e.printStackTrace();
            reportResults(true, ReportLogger.ReportLevel.FAILURE, "Open", "Error",
                    "Cannot access the URL. ::: url : " + url
                            + " ::: Actual Error : " + e.getMessage());
        }

    }

    /**
     * Start of browser session.
     * @param browser 
     */
    private void startOfBrowserSession(String browser) {
        try {
            
            TestObject[] browsers;
            if("Internet Explorer".equalsIgnoreCase(browser)){
                Property[] browserProperties =
                    {new Property(".class", "Html.HtmlBrowser"), new Property(".browserName", "MS Internet Explorer"), new Property(".processName", "iexplore.exe")};
                browsers = findElements(browserProperties);
            }else{
                Property[] browserProperties =
                    {new Property(".class", "Html.HtmlBrowser")};
                browsers = findElements(browserProperties);
            }
            
            if (browsers.length == 0) {
                throw new Exception("No browser found");
            }
            BrowserTestObject testBrowser = new BrowserTestObject(browsers[0]);
            testBrowser.waitForExistence();
            testBrowser.maximize();
            testBrowser.activate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Override.
     * 
     * @param objectName
     *            the object name
     * @param identifier
     *            the identifier
     * @see com.virtusa.isq.rft.runtime.ICommandBase#click(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public final void click(final String objectName, final String identifier) {

        ObjectLocator locator =
                objectMap.getResolvedObjectSearchPath(objectName, identifier);
        doClick(locator);
    }

    /**
     * Override.
     * 
     * @param objectName
     *            the object name
     * @see com.virtusa.isq.rft.runtime.ICommandBase#click(java.lang.String)
     */
    @Override
    public final void click(final String objectName) {

        click(objectName, "");
    }

    /**
     * Do click.
     * 
     * @param locator
     *            the locator
     */
    private void doClick(final ObjectLocator locator) {

        int retryCounter = retry;

        try {
            TestObject[] objectIDArr = findElements(locator.getPropertyArray());
            if (objectIDArr.length > 0) {

                TestObject objectID = objectIDArr[0];
                while (retryCounter > 0) {
                    try {
                        retryCounter--;
                        GuiTestObject element = new GuiTestObject(objectID);
                        element.click();
                        reportResults(ReportLogger.ReportLevel.SUCCESS,
                                "Click", "Success", "Click command passed");
                        break;
                    } catch (Exception ex) {
                        if (!(retryCounter > 0)) {
                            ex.printStackTrace();
                            reportResults(true, 
                                    ReportLogger.ReportLevel.FAILURE,
                                    "Click",
                                    "Error",
                                    "Cannot access the element. ::: "
                                            + "Object : "
                                            + Arrays.asList(locator
                                                    .getPropertyArray())
                                            + " ::: " + "Actual Error : "
                                            + ex.getMessage());
                            break;
                        }
                    }
                }
            } else {
                reportResults(true, 
                        ReportLogger.ReportLevel.FAILURE,
                        "Click",
                        "Error",
                        "Cannot find the element. ::: "
                                + "Object : "
                                + locator.getLogicalName()
                                + " ::: "
                                + "Actual Error : Cannot find the element with properties : "
                                + Arrays.asList(locator.getPropertyArray()));

            }

        } catch (UserAbortedActionException ex) {
            throw ex;
        } catch (Exception ex) {

            ex.printStackTrace();
            reportResults(true, 
                    ReportLogger.ReportLevel.FAILURE,
                    "Click",
                    "Error",
                    "Cannot find the element. ::: " + "Object : "
                            + Arrays.asList(locator.getPropertyArray())
                            + " ::: " + "Actual Error : " + ex.getMessage());
        } finally {
            script.unregisterAll();
        }
    }

    /**
     * Override.
     * 
     * @param objectName
     *            the object name
     * @param inputValue
     *            the input value
     * @see com.virtusa.isq.rft.runtime.ICommandBase#type(java.lang.String,
     *      java.lang.Object)
     */
    @Override
    public final void type(final String objectName, final Object inputValue) {

        type(objectName, "", inputValue);
    }

    /**
     * Override.
     * 
     * @param objectName
     *            the object name
     * @param identifier
     *            the identifier
     * @param inputValue
     *            the input value
     * @see com.virtusa.isq.rft.runtime.ICommandBase#type(java.lang.String,
     *      java.lang.String, java.lang.Object)
     */
    @Override
    public final void type(final String objectName, final String identifier,
            final Object inputValue) {
        ObjectLocator locator =
                objectMap.getResolvedObjectSearchPath(objectName, identifier);
        doType(locator, inputValue);
    }

    /**
     * Do type.
     * 
     * @param locator
     *            the locator
     * @param value
     *            the value
     */
    private void doType(final ObjectLocator locator, final Object value) {

        int retryCounter = retry;

        try {
            TestObject[] objectIDArr = findElements(locator.getPropertyArray());
            if (objectIDArr.length > 0) {

                TestObject objectID = objectIDArr[0];
                while (retryCounter > 0) {
                    try {
                        retryCounter--;
                        TextGuiTestObject element =
                                new TextGuiTestObject(objectID);
                        element.exists();
                        element.setText(value.toString());
                        reportResults(ReportLogger.ReportLevel.SUCCESS, "Type",
                                "Success",
                                "Type command passed. Input value : " + value);
                        break;
                    } catch (Exception ex) {
                        if (!(retryCounter > 0)) {
                            ex.printStackTrace();
                            reportResults(true, 
                                    ReportLogger.ReportLevel.FAILURE,
                                    "Type",
                                    "Error",
                                    "Cannot access the element. ::: Input value : "
                                            + value
                                            + "Object : "
                                            + Arrays.asList(locator
                                                    .getPropertyArray())
                                            + " ::: " + "Actual Error : "
                                            + ex.getMessage());
                            break;
                        }
                    }
                }
            } else {
                reportResults(true, 
                        ReportLogger.ReportLevel.FAILURE,
                        "Type",
                        "Error",
                        "Cannot find the element. ::: Input value : "
                                + value
                                + "Object : "
                                + locator.getLogicalName()
                                + " ::: "
                                + "Actual Error : Cannot find the element with properties : "
                                + Arrays.asList(locator.getPropertyArray()));

            }

        } catch (UserAbortedActionException ex) {
            throw ex;
        } catch (Exception ex) {
            ex.printStackTrace();
            reportResults(true, 
                    ReportLogger.ReportLevel.FAILURE,
                    "Type",
                    "Error",
                    "Cannot find the element. ::: Input value : " + value
                            + " Object : "
                            + Arrays.asList(locator.getPropertyArray())
                            + " ::: " + "Actual Error : " + ex.getMessage());
        } finally {
            script.unregisterAll();
        }
    }

    /**
     * Override.
     * 
     * @param objectName
     *            the object name
     * @param stopOnFailure
     *            the stop on failure
     * @return true, if successful
     * @see com.virtusa.isq.rft.runtime.ICommandBase#checkElementPresent(java.lang.String,
     *      boolean)
     */
    @Override
    public final boolean checkElementPresent(final String objectName,
            final boolean stopOnFailure) {

        return checkElementPresent(objectName, "", stopOnFailure);
    }

    /**
     * Override.
     * 
     * @param objectName
     *            the object name
     * @param identifier
     *            the identifier
     * @param stopOnFailure
     *            the stop on failure
     * @return true, if successful
     * @see com.virtusa.isq.rft.runtime.ICommandBase#checkElementPresent(java.lang.String,
     *      java.lang.String, boolean)
     */
    @Override
    public final boolean checkElementPresent(final String objectName,
            final String identifier, final boolean stopOnFailure) {
        ObjectLocator locator =
                objectMap.getResolvedObjectSearchPath(objectName, identifier);
        return doCheckElementPresent(locator, stopOnFailure);
    }

    /**
     * Do check element present.
     * 
     * @param locator
     *            the locator
     * @param stopOnFailure
     *            the stop on failure
     * @return true, if successful
     */
    private boolean doCheckElementPresent(final ObjectLocator locator,
            final boolean stopOnFailure) {
        boolean isElementPreent = false;
        try {
            TestObject[] objectIDArr = findElements(locator.getPropertyArray());
            if (objectIDArr.length > 0) {
                isElementPreent = true;
                reportResults(ReportLogger.ReportLevel.SUCCESS,
                        "Check Element Present", "Success",
                        "Check Element Present command passed");
            } else {
                
                    reportResults(stopOnFailure, 
                            ReportLogger.ReportLevel.FAILURE,
                            "Check Element Present",
                            "Error",
                            "Cannot find the element. ::: "
                                    + "Object : "
                                    + locator.getLogicalName()
                                    + " ::: "
                                    + "Actual Error : Cannot find the element with properties : "
                                    + Arrays.asList(locator.getPropertyArray()));

            }
        } catch (UserAbortedActionException ex) {
            throw ex;
        } catch (Exception e) {
            e.printStackTrace();
            reportResults(stopOnFailure, 
                    ReportLogger.ReportLevel.FAILURE,
                    "Check Element Present",
                    "Error",
                    "Cannot access the element. ::: " + "Object : "
                            + Arrays.asList(locator.getPropertyArray())
                            + " ::: " + "Actual Error : " + e.getMessage());

        } finally {
            script.unregisterAll();
        }
        return isElementPreent;
    }

    /**
     * Override.
     * 
     * @param objectName
     *            the object name
     * @param identifier
     *            the identifier
     * @return true, if successful
     * @see com.virtusa.isq.rft.runtime.ICommandBase#checkElementPresent(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public final boolean checkElementPresent(final String objectName,
            final String identifier) {

        boolean isElementPreent = false;

        try {
            ObjectLocator locator =
                    objectMap.getResolvedObjectSearchPath(objectName,
                            identifier);
            TestObject[] objectIDArr = findElements(locator.getPropertyArray());
            if (objectIDArr.length > 0) {
                isElementPreent = true;

            } else {
                isElementPreent = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            script.unregisterAll();
        }

        return isElementPreent;
    }

    /**
     * Override.
     * 
     * @param objectName
     *            the object name
     * @param option
     *            the option
     * @see com.virtusa.isq.rft.runtime.ICommandBase#select(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public final void select(final String objectName, final String option) {

        select(objectName, "", option);
    }

    /**
     * Override.
     * 
     * @param objectName
     *            the object name
     * @param identifier
     *            the identifier
     * @param option
     *            the option
     * @see com.virtusa.isq.rft.runtime.ICommandBase#select(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public final void select(final String objectName, final String identifier,
            final String option) {
        ObjectLocator locator =
                objectMap.getResolvedObjectSearchPath(objectName, identifier);
        doSelect(locator, option);
    }

    /**
     * Do select.
     * 
     * @param locator
     *            the locator
     * @param selectOption
     *            the select option
     */
    private void doSelect(final ObjectLocator locator, final String selectOption) {

        int retryCounter = retry;
        String option = selectOption;
        try {
            TestObject[] objectIDArr = findElements(locator.getPropertyArray());
            if (objectIDArr.length > 0) {

                TestObject objectID = objectIDArr[0];
                while (retryCounter > 0) {

                    retryCounter--;
                    SelectGuiSubitemTestObject element =
                            new SelectGuiSubitemTestObject(objectID);
                    TestObject[] options = element.getChildren();
                    ArrayList<String> optionList = getOptionValues(options);
                    if (option.toLowerCase(Locale.US).startsWith("index=")) {

                        option =
                                option.trim().toLowerCase(Locale.US)
                                        .replace("index=", "");
                        int index = Integer.parseInt(option);
                        if (index > 0 && index < optionList.size()) {
                            try {
                                element.select(optionList.get(index));
                                reportResults(ReportLogger.ReportLevel.SUCCESS,
                                        "Select", "Success",
                                        "Select command passed. Input value : "
                                                + selectOption);
                                break;
                            } catch (Exception e) {
                                if (!(retryCounter > 0)) {
                                    e.printStackTrace();
                                    reportResults(true,
                                            ReportLogger.ReportLevel.FAILURE,
                                            "Select",
                                            "Error",
                                            "Cannot access the element. ::: Input value : "
                                                    + selectOption
                                                    + " Object : "
                                                    + Arrays.asList(locator
                                                            .getPropertyArray())
                                                    + "Actual Error : "
                                                    + e.getMessage());
                                    break;
                                }
                            }
                        } else {
                            reportResults(true,
                                    ReportLogger.ReportLevel.FAILURE,
                                    "Select",
                                    "Error",
                                    "Cannot find index "
                                            + option
                                            + " in the actual select element. Input value : "
                                            + selectOption
                                            + " Object : "
                                            + Arrays.asList(locator
                                                    .getPropertyArray())
                                            + "Actual options : " + optionList);
                            break;
                        }

                    } else {
                        if (isPresentInCollection(optionList, option)) {
                            try {
                                element.select(option);
                                reportResults(ReportLogger.ReportLevel.SUCCESS,
                                        "Select", "Success",
                                        "Select command passed. Input value : "
                                                + selectOption);
                                break;
                            } catch (Exception ex) {
                                if (!(retryCounter > 0)) {
                                    ex.printStackTrace();
                                    reportResults(true,
                                            ReportLogger.ReportLevel.FAILURE,
                                            "Select",
                                            "Error",
                                            "Cannot access the element. ::: Input value : "
                                                    + selectOption
                                                    + " Object : "
                                                    + Arrays.asList(locator
                                                            .getPropertyArray())
                                                    + ". " + "Actual Error : "
                                                    + ex.getMessage());
                                    break;
                                }
                            }
                        } else {
                            reportResults(true,
                                    ReportLogger.ReportLevel.FAILURE,
                                    "Select",
                                    "Error",
                                    "Cannot find the option : "
                                            + option
                                            + " in the actual select element. ::: Input value : "
                                            + selectOption
                                            + " Object : "
                                            + Arrays.asList(locator
                                                    .getPropertyArray())
                                            + " ::: " + "Actual options : "
                                            + optionList);
                            break;
                        }
                    }

                }
            } else {
                reportResults(true,
                        ReportLogger.ReportLevel.FAILURE,
                        "Select",
                        "Error",
                        "Cannot find the element. ::: Input value : "
                                + selectOption
                                + " Object : "
                                + locator.getLogicalName()
                                + " ::: "
                                + "Actual Error : Cannot find the element with properties : "
                                + Arrays.asList(locator.getPropertyArray()));

            }

        } catch (UserAbortedActionException ex) {
            throw ex;
        } catch (Exception ex) {
            ex.printStackTrace();
            reportResults(true,
                    ReportLogger.ReportLevel.FAILURE,
                    "Select",
                    "Error",
                    "Cannot find the element. ::: Input value : "
                            + selectOption + " Object : "
                            + Arrays.asList(locator.getPropertyArray())
                            + " ::: " + "Actual Error : " + ex.getMessage());
        } finally {
            script.unregisterAll();
        }
    }

    /**
     * Gets the option values.
     * 
     * @param optionObjArray
     *            the option obj array
     * @return the option values
     */
    private ArrayList<String> getOptionValues(final TestObject[] optionObjArray) {
        ArrayList<String> optionList = new ArrayList<String>();

        for (TestObject optionObj : optionObjArray) {
            try {
                GuiTestObject optionGuiTestObj = new GuiTestObject(optionObj);
                String text = optionGuiTestObj.getProperty(".text").toString();
                if (!"".equals(text) && text != null) {
                    optionList.add(text);
                }
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
        }
        return optionList;
    }

    /**
     * Checks if is present in collection.
     * 
     * @param collection
     *            the collection
     * @param target
     *            the target
     * @return true, if is present in collection
     */
    private boolean isPresentInCollection(final Collection<String> collection,
            final String target) {
        boolean itemFound = false;
        for (String item : collection) {
            if (item.equalsIgnoreCase(target)) {
                itemFound = true;
            }
        }
        return itemFound;
    }

    /**
     * Report results.
     * 
     * @param level
     *            the level
     * @param step
     *            the step
     * @param result
     *            the result
     * @param message
     *            the message
     */
    private void reportResults(final ReportLogger.ReportLevel level,
            final String step, final String result, final String message) {
        reportLogger.logResult(script, level, step, result, message);
    }
    
    private void reportResults(boolean stopOnFailure, final ReportLogger.ReportLevel level,
            final String step, final String result, final String message) {
        reportLogger.logResult(script, level, step, result, message);
        if (stopOnFailure) {
            throw new UserAbortedActionException(message);
        }
    }

    /**
     * Find elements.
     * 
     * @param obj
     *            the obj
     * @return the test object[]
     */
    public final TestObject[] findElements(final Property[] obj) {
        TestObject[] to = null;
        for (int count = 0; count < retry; count++) {
            try {
                System.out.println("Finding Element : " + Arrays.asList(obj));
                RootTestObject root = script.getRootTestObject();
                to = root.find(script.atDescendant(obj));
                if (to.length > 0) {
                    break;
                } else {
                    Utils.pause(retryInterval);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                continue;
            }
        }
        return to;
    }

    /**
     * Override.
     * 
     * @param objectName
     *            the object name
     * @param propertyName
     *            the property name
     * @param expectedValue
     *            the expected value
     * @param stopOnFailure
     *            the stop on failure
     * @see 
     *      com.virtusa.isq.rft.runtime.ICommandBase#checkObjectProperty(java.lang
     *      .String,....)
     */
    @Override
    public final void checkObjectProperty(final String objectName,
            final String propertyName, final String expectedValue,
            final boolean stopOnFailure) {

        checkObjectProperty(objectName, "", propertyName, expectedValue,
                stopOnFailure);

    }

    /**
     * The Enum ObjectValidationType.
     */
    public static enum ObjectValidationType {

        /** The elementpresent. */
        ELEMENTPRESENT,
        /** The propertypresent. */
        PROPERTYPRESENT
    };

    /**
     * Override.
     * 
     * @param objectName
     *            the object name
     * @param identifier
     *            the identifier
     * @param propertyName
     *            the property name
     * @param expectedValue
     *            the expected value
     * @param stopOnFailure
     *            the stop on failure
     * @see 
     *      com.virtusa.isq.rft.runtime.ICommandBase#checkObjectProperty(java.lang
     *      .String, ....)
     */
    @Override
    public final void checkObjectProperty(final String objectName,
            final String identifier, final String propertyName,
            final String expectedValue, final boolean stopOnFailure) {

        ObjectLocator locator =
                objectMap.getResolvedObjectSearchPath(objectName, identifier);
        doCheckObjectProperty(locator, propertyName, expectedValue,
                stopOnFailure);

    }

    /**
     * Do check object property.
     * 
     * @param locator
     *            the locator
     * @param propertyName
     *            the property name
     * @param expectedValue
     *            the expected value
     * @param stopOnFailure
     *            the stop on failure
     */
    private void doCheckObjectProperty(final ObjectLocator locator,
            final String propertyName, final String expectedValue,
            final boolean stopOnFailure) {

        if (propertyName.equals(ObjectValidationType.ELEMENTPRESENT.toString())) {

            doCheckElementNotPresent(locator, propertyName, expectedValue,
                    stopOnFailure);
        } else if (propertyName.equals(ObjectValidationType.PROPERTYPRESENT
                .toString())) {

            throw new NotImplementedException();
        } else {

            doCheckObjectOtherProperty(locator, propertyName, expectedValue,
                    stopOnFailure);
        }
    }

    /**
     * Do check element not present.
     * 
     * @param locator
     *            the locator
     * @param propertyName
     *            the property name
     * @param expectedValue
     *            the expected value
     * @param stopOnFailure
     *            the stop on failure
     */
    private void doCheckElementNotPresent(final ObjectLocator locator,
            final String propertyName, final String expectedValue,
            final boolean stopOnFailure) {

        int retryCounter = retry;
        String objectFound = "false";
        boolean isconditionMatched = false;
        try {
            while (retryCounter > 0) {

                try {
                    retryCounter--;
                    TestObject[] objectIDArr =
                            findElements(locator.getPropertyArray());
                    if (objectIDArr.length > 0) {
                        objectFound = "true";
                    } else {
                        objectFound = "false";
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
                if (objectFound.equalsIgnoreCase(expectedValue)) {
                    isconditionMatched = true;
                    reportResults(ReportLogger.ReportLevel.SUCCESS,
                            "Check Object Property", "Success",
                            "Check Object Property Element Present command passed");
                    break;
                }
            }

            if (!isconditionMatched) {

                
                    reportResults(stopOnFailure,
                            ReportLogger.ReportLevel.FAILURE,
                            "Check Object Property",
                            "Error",
                            "Actual property "
                                    + propertyName
                                    + " condition : "
                                    + isconditionMatched
                                    + " does not match the expected condition : "
                                    + expectedValue + " . ::: " + "Object : "
                                    + locator.getLogicalName());
                

            }

        } finally {
            script.unregisterAll();
        }

    }

    /**
     * Do check object other property.
     * 
     * @param locator
     *            the locator
     * @param propertyName
     *            the property name
     * @param expectedValue
     *            the expected value
     * @param stopOnFailure
     *            the stop on failure
     */
    private void doCheckObjectOtherProperty(final ObjectLocator locator,
            final String propertyName, final String expectedValue,
            final boolean stopOnFailure) {

        int retryCounter = retry;
        try {
            TestObject[] objectIDArr = findElements(locator.getPropertyArray());
            if (objectIDArr.length > 0) {

                TestObject objectID = objectIDArr[0];
                while (retryCounter > 0) {
                    try {
                        retryCounter--;
                        GuiTestObject element = new GuiTestObject(objectID);
                        String actualValue =
                                element.getProperty(propertyName).toString();
                        if (actualValue.equals(expectedValue)) {
                            reportResults(ReportLogger.ReportLevel.SUCCESS,
                                    "Check Object Property", "Success",
                                    "Check Object Property command passed");
                            break;
                        } else {
                            
                                reportResults(stopOnFailure,
                                        ReportLogger.ReportLevel.FAILURE,
                                        "Check Object Property",
                                        "Error",
                                        "Actual property "
                                                + propertyName
                                                + " value : "
                                                + actualValue
                                                + " does not match the expected value : "
                                                + expectedValue + " ."
                                                + "Object : "
                                                + locator.getLogicalName());

                            
                            break;
                        }

                    } catch (UserAbortedActionException ex) {
                        throw ex;
                    } catch (Exception ex) {
                        if (!(retryCounter > 0)) {

                            ex.printStackTrace();
                            reportResults(stopOnFailure,
                                    ReportLogger.ReportLevel.FAILURE,
                                    "Check Object Property",
                                    "Error",
                                    "Cannot access the element. ::: "
                                            + "Object : "
                                            + Arrays.asList(locator
                                                    .getPropertyArray())
                                            + " ::: " + "Actual Error : "
                                            + ex.getMessage());
                            break;
                        }
                    }
                }
            } else {
                reportResults(stopOnFailure,
                        ReportLogger.ReportLevel.FAILURE,
                        "Check Object Property",
                        "Error",
                        "Cannot find the element. ::: "
                                + "Object : "
                                + locator.getLogicalName()
                                + " ::: "
                                + "Actual Error : Cannot find the element with properties : "
                                + Arrays.asList(locator.getPropertyArray()));

            }

        } catch (UserAbortedActionException ex) {
            throw ex;
        } catch (Exception ex) {
            ex.printStackTrace();
            reportResults(stopOnFailure,
                    ReportLogger.ReportLevel.FAILURE,
                    "Check Object Property",
                    "Error",
                    "Cannot find the element. ::: " + "Object : "
                            + Arrays.asList(locator.getPropertyArray())
                            + " ::: " + "Actual Error : " + ex.getMessage());
        } finally {
            script.unregisterAll();
        }

    }

    /**
     * Override.
     * 
     * @param waitTime
     *            the wait time
     * @see com.virtusa.isq.rft.runtime.ICommandBase#pause(java.lang.String)
     */
    @Override
    public final void pause(final String waitTime) {
        try {
            final int time = 1000;
            script.sleep((Double.parseDouble(waitTime)) / time);
            reportResults(ReportLogger.ReportLevel.SUCCESS, "Pause", "Success",
                    "Pause command " + waitTime + "ms passed.");
        } catch (Exception e) {
            reportResults(ReportLogger.ReportLevel.FAILURE, "Pause", "Error",
                    "Pause Command Failed. Actual error : " + e.getMessage());
        }
    }

    
    @Override
    public void navigateToURL(String url, String waitTime) {

        open(url, "", waitTime);
    }

    @Override
    public void navigateToURL(String url, String identifier, String waitTime) {

        String resolvedUrl = objectMap.getResolvedSearchPath(url, identifier);
        doNavigateToURL(resolvedUrl, waitTime);

    }
    
    /**
     * Override.
     * 
     * @param url
     *            the url
     * @param waitTime
     *            the wait time
     * @see com.virtusa.isq.rft.runtime.ICommandBase#navigateToURL(java.lang.String,
     *      java.lang.String)
     */
    private final void doNavigateToURL(final String url, final String waitTime) {

        try {
            RootTestObject root = script.getRootTestObject();
            TestObject[] browsers =
                    root.find(script.atDescendant(".class", "Html.HtmlBrowser"));
            if (browsers.length == 0) {
                throw new Exception("No browser found, after waiting "
                        + waitTime + " seconds");
            }
            BrowserTestObject testBrowser = new BrowserTestObject(browsers[0]);
            testBrowser.loadUrl(url);
            reportResults(ReportLogger.ReportLevel.SUCCESS, "NavigateToURL",
                    "Success", "Navigate To URL command to " + url + " passed.");

        } catch (Exception e) {
            reportResults(true, ReportLogger.ReportLevel.FAILURE, "NavigateToURL",
                    "Error", "Navigate To URL Command Failed. Actual error : "
                            + e.getMessage());
        } finally {
            script.unregisterAll();
        }

    }

    /**
     * Override.
     * 
     * @param objectName
     *            the object name
     * @param identifier
     *            the identifier
     * @param pattern
     *            the pattern
     * @see com.virtusa.isq.rft.runtime.ICommandBase#checkPattern(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public final void checkPattern(final String objectName,
            final String identifier, final String pattern) {
        ObjectLocator locator =
                objectMap.getResolvedObjectSearchPath(objectName, identifier);
        doCheckPattern(locator, pattern);
    }

    /**
     * Override.
     * 
     * @param objectName
     *            the object name
     * @param pattern
     *            the pattern
     * @see com.virtusa.isq.rft.runtime.ICommandBase#checkPattern(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public final void checkPattern(final String objectName, final String pattern) {

        checkPattern(objectName, "", pattern);
    }

    /**
     * Do check pattern.
     * 
     * @param locator
     *            the locator
     * @param pattern
     *            the pattern
     */
    private void doCheckPattern(final ObjectLocator locator,
            final String pattern) {

        int retryCounter = retry;
        try {
            String regex = getRegexPattern(pattern);
            TestObject[] objectIDArr = findElements(locator.getPropertyArray());
            if (objectIDArr.length > 0) {

                TestObject objectID = objectIDArr[0];
                while (retryCounter > 0) {
                    try {
                        retryCounter--;
                        TextGuiTestObject element =
                                new TextGuiTestObject(objectID);

                        String text = element.getText().trim();
                        System.out.println(text);
                        if (text.matches(regex)) {
                            reportResults(ReportLogger.ReportLevel.SUCCESS,
                                    "Check Pattern", "Success",
                                    "Check Pattern command passed");
                            break;
                        } else {
                            reportResults(true, 
                                    ReportLogger.ReportLevel.FAILURE,
                                    "Check Pattern",
                                    "Error",
                                    "The actual value : "
                                            + text
                                            + " does not match the regex : "
                                            + regex
                                            + "Object : "
                                            + Arrays.asList(locator
                                                    .getPropertyArray())
                                            + " ::: ");
                            break;
                        }

                    } catch (UserAbortedActionException ex) {
                        throw ex;
                    } catch (Exception ex) {

                        if (!(retryCounter > 0)) {
                            ex.printStackTrace();
                            reportResults(true, 
                                    ReportLogger.ReportLevel.FAILURE,
                                    "Check Pattern",
                                    "Error",
                                    "Cannot access the element. ::: "
                                            + "Object : "
                                            + Arrays.asList(locator
                                                    .getPropertyArray())
                                            + " ::: " + "Actual Error : "
                                            + ex.getMessage());
                            break;
                        }
                    }
                }
            } else {
                reportResults(true, 
                        ReportLogger.ReportLevel.FAILURE,
                        "Check Pattern",
                        "Error",
                        "Cannot find the element. ::: "
                                + "Object : "
                                + locator.getLogicalName()
                                + " ::: "
                                + "Actual Error : Cannot find the element with properties : "
                                + Arrays.asList(locator.getPropertyArray()));

            }

        } catch (UserAbortedActionException ex) {
            throw ex;
        } catch (Exception ex) {

            ex.printStackTrace();
            reportResults(true, ReportLogger.ReportLevel.FAILURE, "Check Pattern",
                    "Error", "Cannot find the element. ::: " + "Object : "
                            + Arrays.asList(locator.getPropertyArray())
                            + " ::: " + "Actual Error : " + ex.getMessage());
        } finally {
            script.unregisterAll();
        }
    }

    /**
     * Gets the regex pattern.
     * 
     * @param patternString
     *            the pattern string
     * @return the regex pattern
     */
    private String getRegexPattern(final String patternString) {

        String regex = "";
        String pattern = patternString;
        if (pattern.toLowerCase(Locale.getDefault()).startsWith("regex=")) {
            pattern =
                    pattern.substring(pattern.indexOf('=') + 1,
                            pattern.length());
            regex = pattern;
        } else {
            char[] patternChars = pattern.toCharArray();
            StringBuilder regexBuilder = new StringBuilder();
            for (int strIndex = 0; strIndex < patternChars.length; strIndex++) {

                if (patternChars[strIndex] == 'S') {
                    regexBuilder.append("[A-Z]");
                } else if (patternChars[strIndex] == 's') {
                    regexBuilder.append("[a-z]");
                } else if (patternChars[strIndex] == 'd') {
                    regexBuilder.append("\\d");
                } else {
                    regexBuilder.append(patternChars[strIndex]);
                }
            }
            regex = regexBuilder.toString();
        }
        return regex;
    }

    /**
     * Stores a given key-value pair of given type <br>
     * Overwrites any existing value of same key <br>
     * <br>
     * <b>Fails</b> if, <li>data store file cannot be created</li> <li>data
     * cannot be written to file</li> <li>type of the value to be stored
     * mismatches the type specified</li> <br>
     * <br>
     * .
     * 
     * @param key
     *            : key for the value to be stored
     * @param type
     *            : type of value to be stored
     * @param value
     *            the value
     */
    @Override
    public final void store(final String key, final String type,
            final String value) {
        String projectPropertiesLocation = "project_data.properties";
        Properties prop = new Properties();
        FileInputStream fis = null;
        FileOutputStream fos = null;
        File file = new File(projectPropertiesLocation);
        try {
            if (!file.exists() && !file.createNewFile()) {

                System.err
                        .println("Cannot create a new file in the intended location. "
                                + "" + file.getAbsolutePath());
            }
            fis = new FileInputStream(file.getAbsoluteFile());
            prop.load(fis);
            prop.setProperty(key + "_Val", value);
            prop.setProperty(key + "_Type", type);

            checkStoreValueType(type, value);

            fos = new FileOutputStream(projectPropertiesLocation);
            prop.store(fos, "project settings");
            reportResults(ReportLogger.ReportLevel.SUCCESS, "Store", "Success",
                    "Store value passed. Input value : " + value);

        } catch (IOException e) {
            reportResults(true, ReportLogger.ReportLevel.FAILURE, "Store", "Error",
                    "Cannot Store the value. ::: " + value + " : " + type
                            + " : " + key + "Actual Error : " + e.getMessage());

        } catch (NumberFormatException e) {
            reportResults(true, ReportLogger.ReportLevel.FAILURE, "Store", "Error",
                    "Cannot Store the value. ::: " + value + " : " + type
                            + " : " + key + "Actual Error : " + e.getMessage());
        } catch (IllegalArgumentException e) {

            reportResults(true, ReportLogger.ReportLevel.FAILURE, "Store", "Error",
                    "Cannot parse value to the expected format. ::: Actual Error : "
                            + e.getMessage());

        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Check store value type.
     * 
     * @param type
     *            the type
     * @param value
     *            the value
     */
    private void checkStoreValueType(final String type, final String value) {
        try {
            if ("Int".equalsIgnoreCase(type)) {
                Integer.parseInt(value);
            } else if ("Boolean".equalsIgnoreCase(type)) {
                if ("true".equalsIgnoreCase(value)
                        || "false".equalsIgnoreCase(value)) {
                    Boolean.parseBoolean(value);
                } else {
                    throw new IllegalArgumentException(
                            "Cannot convert to boolean value " + value);
                }
            } else if ("Float".equalsIgnoreCase(type)) {
                Float.parseFloat(value);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    /**
     * Retrieves a String value previously stored.
     * 
     * @param key
     *            : key for the value to be retrieved
     * @return String value stored for the given <b>key</b>
     */
    @Override
    public final String retrieveString(final String key) {
        return retrieve(key, "String");
    }

    /**
     * Retrieve the value of a given key previously stored <br>
     * <br>
     * <b>Fails</b> if, <li>the given key is not stored previously</li> <li>
     * stored value type mismatches the type expected</li> <br>
     * <br>
     * .
     * 
     * @param key
     *            : key for the value to be retrieved
     * @param type
     *            : type of the previously stored value
     * @return value for the particular <b>key</b>
     */
    private String retrieve(final String key, final String type) {

        String value = null;
        String projectPropertiesLocation = "project_data.properties";
        Properties prop = new Properties();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(projectPropertiesLocation);
            try {
                prop.load(fis);
            } catch (IOException e) {

                reportResults(true, ReportLogger.ReportLevel.FAILURE, "Retrieve",
                        "Error", "Cannot retrieve value. " + type + " " + key
                                + "::: Actual Error : " + e.getMessage());
            }

            value = prop.getProperty(key + "_Val");
            if (value != null) {
                String type2 = prop.getProperty(key + "_Type");
                if (!type2.equalsIgnoreCase(type)) {

                    reportResults(true, 
                            ReportLogger.ReportLevel.FAILURE,
                            "Retrieve",
                            "Error",
                            "Cannot retrieve value. "
                                    + type
                                    + " "
                                    + key
                                    + "::: Actual Error : Trying to retrieve : "
                                    + type + ", found : " + type2);

                }
            }

            reportResults(true, ReportLogger.ReportLevel.SUCCESS, "Retrieve",
                    "Success", "Retrieve value passed. Retrieve value : "
                            + value);

        } catch (FileNotFoundException e) {

            reportResults(true, ReportLogger.ReportLevel.FAILURE, "Retrieve",
                    "Error", "Cannot retrieve value. " + type + " " + key
                            + "::: Actual Error : " + e.getMessage());
            return null;

        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return value;
    }

    /**
     * Retrieves an int value previously stored <br>
     * <br>
     * <b>Fails</b> if, <li>the stored value is not parsable to int</li> <br>
     * <br>
     * .
     * 
     * @param key
     *            : key for the value to be retrieved
     * @return int value stored for the given <b>key</b> , default is -1
     */
    @Override
    public final int retrieveInt(final String key) {
        String value = retrieve(key, "Int");
        try {
            if (value != null) {
                return Integer.parseInt(value);
            }
        } catch (NumberFormatException e) {
            reportResults(
                    ReportLogger.ReportLevel.FAILURE,
                    "Retrieve",
                    "Error",
                    "Cannot parse the retrieved value to Integer. Value : "
                            + value + " " + key + "::: Actual Error : "
                            + e.getMessage());
        }
        return -1;
    }

    /**
     * Retrieves a float value previously stored <br>
     * <br>
     * <b>Fails</b> if, <li>the stored value is not parsable to float</li> <br>
     * <br>
     * .
     * 
     * @param key
     *            : key for the value to be retrieved
     * @return float value stored for the given <b>key</b> , default is -1
     */
    @Override
    public final float retrieveFloat(final String key) {
        String value = retrieve(key, "Float");
        try {
            if (value != null) {
                return Float.parseFloat(value);
            }
        } catch (NumberFormatException e) {

            reportResults(true, 
                    ReportLogger.ReportLevel.FAILURE,
                    "Retrieve",
                    "Error",
                    "Cannot parse the retrieved value to Float. Value : "
                            + value + " " + key + "::: Actual Error : "
                            + e.getMessage());
        }
        return -1;
    }

    /**
     * Retrieves a boolean value previously stored <br>
     * <br>
     * <b>Fails</b> if, <li>the stored value is not parsable to boolean</li> <br>
     * <br>
     * .
     * 
     * @param key
     *            : key for the value to be retrieved
     * @return boolean value stored for the given <b>key</b> , default is false
     */
    @Override
    public final boolean retrieveBoolean(final String key) {
        String value = retrieve(key, "Boolean");
        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return Boolean.parseBoolean(value);
        } else {

            reportResults(true, ReportLogger.ReportLevel.FAILURE, "Retrieve",
                    "Error",
                    "Cannot parse the retrieved value to Boolean. Value : "
                            + value + " " + key);

            return false;
        }
    }

    /**
     * Override.
     * 
     * @param objectName
     *            the object name
     * @param identifier
     *            the identifier
     * @param coordinates
     *            the coordinates
     * @see com.virtusa.isq.rft.runtime.ICommandBase#click(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public final void clickAt(final String objectName, final String identifier,
            final String coordinates) {

        ObjectLocator locator =
                objectMap.getResolvedObjectSearchPath(objectName, identifier);
        doClickAt(locator, coordinates);
    }

    /**
     * Override.
     * 
     * @param objectName
     *            the object name
     * @param coordinates
     *            the coordinates
     * @see com.virtusa.isq.rft.runtime.ICommandBase#click(java.lang.String)
     */
    @Override
    public final void clickAt(final String objectName, final String coordinates) {

        clickAt(objectName, "", coordinates);
    }

    /**
     * Do click.
     * 
     * @param locator
     *            the locator
     * @param coordinates
     *            the coordinates
     */
    private void doClickAt(final ObjectLocator locator, final String coordinates) {

        int retryCounter = retry;
        int xOffset = 0;
        int yOffset = 0;
        try {
            try {
                xOffset = Integer.parseInt((coordinates.split(",")[0]).trim());
                yOffset = Integer.parseInt((coordinates.split(",")[1]).trim());
            } catch (Exception e) {
                reportResults(true, ReportLogger.ReportLevel.FAILURE, "Click At",
                        "Error", "Cannot parse the coordinates " + coordinates
                                + " for the command. ::: " + "Object : "
                                + Arrays.asList(locator.getPropertyArray())
                                + "Actual Error : " + e.getMessage());

            }
            TestObject[] objectIDArr = findElements(locator.getPropertyArray());
            if (objectIDArr.length > 0) {

                TestObject objectID = objectIDArr[0];
                while (retryCounter > 0) {
                    try {
                        retryCounter--;
                        GuiTestObject element = new GuiTestObject(objectID);
                        Point p = new Point(xOffset, yOffset);
                        element.click(p);
                        reportResults(ReportLogger.ReportLevel.SUCCESS,
                                "Click At", "Success",
                                "Click At command passed");
                        break;
                    } catch (Exception ex) {
                        if (!(retryCounter > 0)) {
                            ex.printStackTrace();
                            reportResults(true, 
                                    ReportLogger.ReportLevel.FAILURE,
                                    "Click At",
                                    "Error",
                                    "Cannot access the element. ::: "
                                            + "Object : "
                                            + Arrays.asList(locator
                                                    .getPropertyArray())
                                            + " ::: " + "Actual Error : "
                                            + ex.getMessage());
                            break;
                        }
                    }
                }
            } else {
                reportResults(true, 
                        ReportLogger.ReportLevel.FAILURE,
                        "Click At",
                        "Error",
                        "Cannot find the element. ::: "
                                + "Object : "
                                + locator.getLogicalName()
                                + " ::: "
                                + "Actual Error : Cannot find the element with properties : "
                                + Arrays.asList(locator.getPropertyArray()));

            }

        } catch (UserAbortedActionException uaae) {
            throw uaae;
        } catch (Exception ex) {
            ex.printStackTrace();
            reportResults(true, ReportLogger.ReportLevel.FAILURE, "Click At",
                    "Error", "Cannot find the element. ::: " + "Object : "
                            + Arrays.asList(locator.getPropertyArray())
                            + " ::: " + "Actual Error : " + ex.getMessage());
        } finally {
            script.unregisterAll();
        }
    }

    /**
     * Override.
     * 
     * @param objectName
     *            the object name
     * @param identifier
     *            the identifier
     * @param coordinates
     *            the coordinates
     * @see com.virtusa.isq.rft.runtime.ICommandBase#click(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public final void doubleClickAt(final String objectName,
            final String identifier, final String coordinates) {

        ObjectLocator locator =
                objectMap.getResolvedObjectSearchPath(objectName, identifier);
        doDoubleClickAt(locator, coordinates);
    }

    /**
     * Override.
     * 
     * @param objectName
     *            the object name
     * @param coordinates
     *            the coordinates
     * @see com.virtusa.isq.rft.runtime.ICommandBase#click(java.lang.String)
     */
    @Override
    public final void doubleClickAt(final String objectName,
            final String coordinates) {

        doubleClickAt(objectName, "", coordinates);
    }

    /**
     * Do click.
     * 
     * @param locator
     *            the locator
     * @param coordinates
     *            the coordinates
     */
    private void doDoubleClickAt(final ObjectLocator locator,
            final String coordinates) {

        int retryCounter = retry;
        int xOffset = 0;
        int yOffset = 0;
        try {
            try {
                xOffset = Integer.parseInt((coordinates.split(",")[0]).trim());
                yOffset = Integer.parseInt((coordinates.split(",")[1]).trim());
            } catch (Exception e) {
                reportResults(true, ReportLogger.ReportLevel.FAILURE, "Click At",
                        "Error", "Cannot parse the coordinates " + coordinates
                                + " for the command. ::: " + "Object : "
                                + Arrays.asList(locator.getPropertyArray())
                                + "Actual Error : " + e.getMessage());

            }
            TestObject[] objectIDArr = findElements(locator.getPropertyArray());
            if (objectIDArr.length > 0) {

                TestObject objectID = objectIDArr[0];
                while (retryCounter > 0) {
                    try {
                        retryCounter--;
                        GuiTestObject element = new GuiTestObject(objectID);
                        Point p = new Point(xOffset, yOffset);
                        element.doubleClick(p);
                        reportResults(ReportLogger.ReportLevel.SUCCESS,
                                "Double Click At", "Success",
                                "Click At command passed");
                        break;
                    } catch (Exception ex) {
                        if (!(retryCounter > 0)) {
                            ex.printStackTrace();
                            reportResults(true, 
                                    ReportLogger.ReportLevel.FAILURE,
                                    "Double Click At",
                                    "Error",
                                    "Cannot access the element. ::: "
                                            + "Object : "
                                            + Arrays.asList(locator
                                                    .getPropertyArray())
                                            + " ::: " + "Actual Error : "
                                            + ex.getMessage());
                            break;
                        }
                    }
                }
            } else {
                reportResults(true, 
                        ReportLogger.ReportLevel.FAILURE,
                        "Double Click At",
                        "Error",
                        "Cannot find the element. ::: "
                                + "Object : "
                                + locator.getLogicalName()
                                + " ::: "
                                + "Actual Error : Cannot find the element with properties : "
                                + Arrays.asList(locator.getPropertyArray()));

            }

        } catch (UserAbortedActionException ex) {
            throw ex;
        } catch (Exception ex) {

            ex.printStackTrace();
            reportResults(true, ReportLogger.ReportLevel.FAILURE, "Double Click At",
                    "Error", "Cannot find the element. ::: " + "Object : "
                            + Arrays.asList(locator.getPropertyArray())
                            + " ::: " + "Actual Error : " + ex.getMessage());
        } finally {
            script.unregisterAll();
        }
    }

    /**
     * Override.
     * 
     * @param objectName
     *            the object name
     * @param identifier
     *            the identifier
     * @see com.virtusa.isq.rft.runtime.ICommandBase#click(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public final void doubleClick(final String objectName,
            final String identifier) {

        ObjectLocator locator =
                objectMap.getResolvedObjectSearchPath(objectName, identifier);
        doDoubleClick(locator);
    }

    /**
     * Override.
     * 
     * @param objectName
     *            the object name
     * @see com.virtusa.isq.rft.runtime.ICommandBase#click(java.lang.String)
     */
    @Override
    public final void doubleClick(final String objectName) {

        click(objectName, "");
    }

    /**
     * Do click.
     * 
     * @param locator
     *            the locator
     */
    private void doDoubleClick(final ObjectLocator locator) {

        int retryCounter = retry;

        try {
            TestObject[] objectIDArr = findElements(locator.getPropertyArray());
            if (objectIDArr.length > 0) {

                TestObject objectID = objectIDArr[0];
                while (retryCounter > 0) {
                    try {
                        retryCounter--;
                        GuiTestObject element = new GuiTestObject(objectID);
                        element.doubleClick();
                        reportResults(ReportLogger.ReportLevel.SUCCESS,
                                "Double Click", "Success",
                                "Double Click command passed");
                        break;
                    } catch (Exception ex) {
                        if (!(retryCounter > 0)) {
                            ex.printStackTrace();
                            reportResults(true, 
                                    ReportLogger.ReportLevel.FAILURE,
                                    "Double Click",
                                    "Error",
                                    "Cannot access the element. ::: "
                                            + "Object : "
                                            + Arrays.asList(locator
                                                    .getPropertyArray())
                                            + " ::: " + "Actual Error : "
                                            + ex.getMessage());
                            break;
                        }
                    }
                }
            } else {
                reportResults(true, 
                        ReportLogger.ReportLevel.FAILURE,
                        "Double Click",
                        "Error",
                        "Cannot find the element. ::: "
                                + "Object : "
                                + locator.getLogicalName()
                                + " ::: "
                                + "Actual Error : Cannot find the element with properties : "
                                + Arrays.asList(locator.getPropertyArray()));

            }

        } catch (UserAbortedActionException ex) {
            throw ex;
        } catch (Exception ex) {
            ex.printStackTrace();
            reportResults(true, ReportLogger.ReportLevel.FAILURE, "Double Click",
                    "Error", "Cannot find the element. ::: " + "Object : "
                            + Arrays.asList(locator.getPropertyArray())
                            + " ::: " + "Actual Error : " + ex.getMessage());
        } finally {
            script.unregisterAll();
        }
    }

    /**
     * Fail.
     * 
     * @param message
     *            the message
     */
    @Override
    public final void fail(final Object message) {
        reportResults(true, ReportLogger.ReportLevel.FAILURE, "Fail", "Error",
                "Fail message : " + message);
    }

    /**
     * Fires a native robot event into the webpage. <br>
     * 
     * @param event
     *            : Specicy the event which should be performed<br>
     *            1. If a keyboard event event should be started with KEY%<br>
     * <br>
     *            Ex: KEY%\n|\t<br>
     * <br>
     *            2. If it is a mouse event event should be started with MOUSE%<br>
     * <br>
     *            Ex: MOUSE%CLICK|RCLICK <br>
     * <br>
     * @param waittime
     *            : Wait time before the events.
     * */
    @Override
    public final void fireEvent(final String event, final String waittime) {

        Utils.pause(Integer.parseInt(waittime));
        /*
         * START DESCRIPTION following for loop was added to make the command
         * more consistent try the command for give amount of time (can be
         * configured through class variable RETRY) command will be tried for
         * "RETRY" amount of times or until command works. any exception thrown
         * within the tries will be handled internally.
         * 
         * can be exited from the loop under 2 conditions 1. if the command
         * succeeded 2. if the RETRY count is exceeded
         */
        try {
            if (event.startsWith("KEY%")) {

                fireKeyEvent(event.split("%")[1]);
            } else if (event.startsWith("MOUSE%")) {

                fireMouseEvent(event.split("%")[1]);
            } else if (event.startsWith("VERIFY%")) {

                fireEventVerifyValue(event.split("%")[1]);
            } else {
                reportResults(true, ReportLogger.ReportLevel.FAILURE, "Fire Event",
                        "Error", "Cannot perform the event. ::: "
                                + "Actual Error : Invalid event type passed "
                                + event);
            }

            reportResults(ReportLogger.ReportLevel.SUCCESS, "Fire Event",
                    "Success", "Performed the event successfully. Event : "
                            + event);

        } catch (Exception e) {

            if (e.getMessage().startsWith("Command")) {
                e.printStackTrace();
                reportResults(true, ReportLogger.ReportLevel.FAILURE, "Fire Event",
                        "Error", "Cannot perform the invalid event. ::: "
                                + "Actual Error : Invalid event type passed "
                                + event);
            } else {
                e.printStackTrace();
                reportResults(true, ReportLogger.ReportLevel.FAILURE, "Fire Event",
                        "Error", "Cannot perform the event. ::: "
                                + "Actual Error : " + e.getMessage());
            }
        }
    }

    /**
     * Get the selected text in webpage to the clipboard and compare the value
     * with the given input.
     * 
     * @param value
     *            the value
     * @throws Exception
     *             the exception
     */

    private void fireEventVerifyValue(final String value) throws Exception {

        String clipBoardText = "";
        Robot robot = new Robot();

        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_C);
        robot.keyRelease(KeyEvent.VK_C);
        robot.keyRelease(KeyEvent.VK_CONTROL);

        Utils.pause(retryInterval);
        Transferable trans =
                Toolkit.getDefaultToolkit().getSystemClipboard()
                        .getContents(null);

        try {
            if (trans != null) {
                clipBoardText =
                        (String) trans.getTransferData(DataFlavor.stringFlavor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (clipBoardText.equals(value)) {

            reportResults(ReportLogger.ReportLevel.SUCCESS, "Fire Event",
                    "Success", "Verify value passed. Value : " + value);

        } else {

            reportResults(true, ReportLogger.ReportLevel.FAILURE, "Fire Event",
                    "Error", "Verify value match expected. ::: "
                            + "Expected value : " + value + " Actual value : "
                            + clipBoardText);
        }
    }

    /**
     * Fires a set of java robot key events into the webpage.
     * 
     * @param commands
     *            the commands
     * @throws Exception
     *             the exception
     */

    private void fireKeyEvent(final String commands) throws Exception {

        String[] commandSet = commands.split("\\|");
        Robot robot = new Robot();
        for (String fullCommand : commandSet) {
            Utils.pause(retryInterval / 2);
            int commandIndex = 0;
            int inputIndex = 1;
            String command = fullCommand.split("=")[commandIndex];
            String input = fullCommand.split("=")[inputIndex];
            if ("type".equalsIgnoreCase(command)) {
                Clipboard clipboard =
                        Toolkit.getDefaultToolkit().getSystemClipboard();
                StringSelection stringSelection = new StringSelection(input);
                clipboard.setContents(stringSelection, null);

                robot.keyPress(KeyEvent.VK_CONTROL);
                robot.keyPress(KeyEvent.VK_V);
                robot.keyRelease(KeyEvent.VK_V);
                robot.keyRelease(KeyEvent.VK_CONTROL);

            } else if ("Key".equalsIgnoreCase(command)) {

                type(input);
            } else if ("wait".equalsIgnoreCase(command)) {

                Utils.pause(Integer.parseInt(input));
            } else {
                throw new Exception("Command " + command);
            }
        }
    }

    /**
     * Fires a set of java robot mouse events into the webpage.
     * 
     * @param commands
     *            the commands
     * @throws Exception
     *             the exception
     */

    private void fireMouseEvent(final String commands) throws Exception {

        String[] commandSet = commands.split("\\|");
        Robot robot = new Robot();
        final int optimumPauseBetweenKeyCombs = 10;
        final int f11KeyCode = KeyEvent.VK_F11;
        for (String fullCommand : commandSet) {
            Utils.pause(retryInterval);
            int commandIndex = 0;
            int inputIndex = 1;
            String command = fullCommand.split("=")[commandIndex];
            String input = fullCommand.split("=")[inputIndex];

            if ("MOVE".equalsIgnoreCase(command)) {

                String[] coords = input.split(",");
                int resolutionWidth = Integer.parseInt(coords[0]);
                int resolutionHeight = Integer.parseInt(coords[inputIndex]);
                int x = Integer.parseInt(coords[inputIndex + 1]);
                int y = Integer.parseInt(coords[inputIndex + 2]);

                int xCordinateAutual = (int) calWidth(resolutionWidth, x);
                int yCordinateAutual = (int) calHight(resolutionHeight, y);

                robot.keyPress(f11KeyCode);
                robot.delay(optimumPauseBetweenKeyCombs);
                robot.keyRelease(f11KeyCode);
                Utils.pause(retryInterval);

                // Mouse Move
                robot.mouseMove(xCordinateAutual, yCordinateAutual);

                robot.keyPress(f11KeyCode);
                Utils.pause(optimumPauseBetweenKeyCombs);
                robot.keyRelease(f11KeyCode);

            } else if ("SCROLL".equalsIgnoreCase(command)) {

                robot.mouseWheel(Integer.parseInt(input));

            } else if ("wait".equalsIgnoreCase(command)) {

                Utils.pause(Integer.parseInt(input));
            } else {
                throw new Exception("Command " + command);
            }
        }
    }

    /**
     * Type.
     * 
     * @param character
     *            the character
     */
    private void type(final String character) {

        KeyCodes keys = new KeyCodes();
        doTypeKeyCodes(keys.getKeyCodes(character));
    }

    /**
     * Do type.
     * 
     * @param keyCodes
     *            the key codes
     */
    private void doTypeKeyCodes(final int... keyCodes) {
        doTypeKeys(keyCodes, 0, keyCodes.length);
    }

    /**
     * Do type.
     * 
     * @param keyCodes
     *            the key codes
     * @param offset
     *            the offset
     * @param length
     *            the length
     */
    private void doTypeKeys(final int[] keyCodes, final int offset,
            final int length) {
        if (length == 0) {
            return;
        }
        try {
            Robot robot = new Robot();
            robot.keyPress(keyCodes[offset]);
            doTypeKeys(keyCodes, offset + 1, length - 1);
            robot.keyRelease(keyCodes[offset]);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Support method for mouseMoveAndClick Calculate the width of the test
     * runner PC.
     * 
     * @param oldSystemWidth
     *            the old system width
     * @param oldSystemX
     *            the old system x
     * @return the double
     */

    private static double calWidth(final double oldSystemWidth,
            final double oldSystemX) {
        double newSystemWidth = resizeScreen().width;
        return (oldSystemX / oldSystemWidth) * newSystemWidth;

    }

    /**
     * Support method for mouseMoveAndClick <br>
     * Calculate the height of the test runner PC.
     * 
     * @param oldSystemHigh
     *            the old system high
     * @param oldSystemY
     *            the old system y
     * @return the double
     */
    private static double calHight(final double oldSystemHigh,
            final double oldSystemY) {
        double newSystemHigh = resizeScreen().height;
        return (oldSystemY / oldSystemHigh) * newSystemHigh;
    }

    /**
     * Support method for mouseMoveAndClick <br>
     * Resize the screen.
     * 
     * @return the dimension
     */
    private static Dimension resizeScreen() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        return toolkit.getScreenSize();
    }

    /**
     * Override.
     * 
     * @param objectName
     *            the object name
     * @param inputValue
     *            the input value
     * @see com.virtusa.isq.rft.runtime.ICommandBase#keyPress(java.lang.String,
     *      java.lang.Object)
     */
    @Override
    public final void keyPress(final String objectName, final Object inputValue) {

        keyPress(objectName, "", inputValue);
    }

    /**
     * Simulates a keypress in to an input field as though you typed it key by
     * key from the keyboard.<br>
     * The keys should be seperated form a | charater to be typed correctly.<br>
     * <br>
     * 
     * Example: A|B|C|ctrl|\n|\t|1|2|3 <br>
     * <br>
     * 
     * @param objectName
     *            the object name
     * @param identifier
     *            Identifier is us to increase the reusablity of the locator.
     *            The usage can be defined using the following examble <br>
     * <br>
     *            assume the following locator is assigned the following logical
     *            object name at the object map <br>
     * <br>
     *            <b>locator :</b> //a[@href='http://www.virtusa.com/']<br>
     *            <b>Name :</b> virtusaLink<br>
     * <br>
     * 
     *            If the user thinks that the locator can be made generalized,
     *            it can be parameterized like the following <br>
     * <br>
     *            //a[@href='http://&LTp1&GT/']<br>
     * <br>
     *            once the method is used, pass the <b>identifier</b> as follows<br>
     *            p1: www.virtusa.com<br>
     * <br>
     *            The absolute xpath will be dynamically generated
     * @param inputValue
     *            the input value
     * @see com.virtusa.isq.rft.runtime.ICommandBase#keyPress(java.lang.String,
     *      java.lang.String, java.lang.Object)
     */
    @Override
    public final void keyPress(final String objectName,
            final String identifier, final Object inputValue) {
        ObjectLocator locator =
                objectMap.getResolvedObjectSearchPath(objectName, identifier);
        doKeyPress(locator, inputValue);
    }

    /**
     * Simulates a keypress in to an input field as though you typed it key by
     * key from the keyboard.<br>
     * The keys should be seperated form a | charater to be typed correctly.<br>
     * <br>
     * 
     * Example: A|B|C|ctrl|\n|\t|1|2|3 <br>
     * <br>
     * 
     * @param locator
     *            the locator
     * @param value
     *            the value
     */
    private void doKeyPress(final ObjectLocator locator, final Object value) {

        int retryCounter = retry;
        try {
            String[] valueStringsArr = value.toString().split("\\|");
            TestObject[] objectIDArr = findElements(locator.getPropertyArray());
            if (objectIDArr.length > 0) {

                TestObject objectID = objectIDArr[0];
                while (retryCounter > 0) {
                    try {
                        retryCounter--;
                        GuiTestObject element = new GuiTestObject(objectID);
                        element.hover();
                        for (int strLocation = 0; strLocation < valueStringsArr.length; strLocation++) {
                            if (!valueStringsArr[strLocation].isEmpty()) {
                                Utils.pause(Integer.parseInt("1000"));
                                type(valueStringsArr[strLocation]);
                            }
                        }

                        reportResults(ReportLogger.ReportLevel.SUCCESS,
                                "Key Press", "Success",
                                "Key Press command passed");
                        break;
                    } catch (Exception ex) {
                        if (!(retryCounter > 0)) {
                            ex.printStackTrace();
                            reportResults(true, 
                                    ReportLogger.ReportLevel.FAILURE,
                                    "Key Press",
                                    "Error",
                                    "Cannot press the given key inputs : "+Arrays.asList(valueStringsArr)+" into the object. ::: "
                                            + "Object : "
                                            + Arrays.asList(locator
                                                    .getPropertyArray())
                                            + " ::: " + "Actual Error : "
                                            + ex.getMessage());
                            break;
                        }
                    }
                }
            } else {
                reportResults(true, 
                        ReportLogger.ReportLevel.FAILURE,
                        "Key Press",
                        "Error",
                        "Cannot find the element. ::: "
                                + "Object : "
                                + locator.getLogicalName()
                                + " ::: "
                                + "Actual Error : Cannot find the element with properties : "
                                + Arrays.asList(locator.getPropertyArray()));

            }

        } catch (UserAbortedActionException ex) {
            throw ex;
        } catch (Exception ex) {
            ex.printStackTrace();
            reportResults(true, ReportLogger.ReportLevel.FAILURE, "Key Press",
                    "Error", "Cannot find the element. ::: " + "Object : "
                            + Arrays.asList(locator.getPropertyArray())
                            + " ::: " + "Actual Error : " + ex.getMessage());
        } finally {
            script.unregisterAll();
        }
    }

    /**
     * Override.
     * 
     * @param objectName
     *            the object name
     * @param identifier
     *            the identifier
     * @see com.virtusa.isq.rft.runtime.ICommandBase#click(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public final void mouseOver(final String objectName, final String identifier) {

        ObjectLocator locator =
                objectMap.getResolvedObjectSearchPath(objectName, identifier);
        doMouseOver(locator);
    }

    /**
     * Override.
     * 
     * @param objectName
     *            the object name
     * @see com.virtusa.isq.rft.runtime.ICommandBase#click(java.lang.String)
     */
    @Override
    public final void mouseOver(final String objectName) {

        mouseOver(objectName, "");
    }

    /**
     * Do click.
     * 
     * @param locator
     *            the locator
     */
    private void doMouseOver(final ObjectLocator locator) {

        int retryCounter = retry;

        try {
            TestObject[] objectIDArr = findElements(locator.getPropertyArray());
            if (objectIDArr.length > 0) {

                TestObject objectID = objectIDArr[0];
                while (retryCounter > 0) {
                    try {
                        retryCounter--;
                        GuiTestObject element = new GuiTestObject(objectID);
                        element.hover();
                        reportResults(ReportLogger.ReportLevel.SUCCESS,
                                "Mouse Over", "Success",
                                "Mouse Over command passed");
                        break;
                    } catch (Exception ex) {
                        if (!(retryCounter > 0)) {
                            ex.printStackTrace();
                            reportResults(true, 
                                    ReportLogger.ReportLevel.FAILURE,
                                    "Mouse Over",
                                    "Error",
                                    "Cannot access the element. ::: "
                                            + "Object : "
                                            + Arrays.asList(locator
                                                    .getPropertyArray())
                                            + " ::: " + "Actual Error : "
                                            + ex.getMessage());
                            break;
                        }
                    }
                }
            } else {
                reportResults(true, 
                        ReportLogger.ReportLevel.FAILURE,
                        "Mouse Over",
                        "Error",
                        "Cannot find the element. ::: "
                                + "Object : "
                                + locator.getLogicalName()
                                + " ::: "
                                + "Actual Error : Cannot find the element with properties : "
                                + Arrays.asList(locator.getPropertyArray()));

            }

        } catch (Exception ex) {
            if (ex instanceof UserAbortedActionException) {
                throw ex;
            }
            ex.printStackTrace();
            reportResults(true, ReportLogger.ReportLevel.FAILURE, "Mouse Over",
                    "Error", "Cannot find the element. ::: " + "Object : "
                            + Arrays.asList(locator.getPropertyArray())
                            + " ::: " + "Actual Error : " + ex.getMessage());
        } finally {
            script.unregisterAll();
        }
    }

    /**
     * Simulates the back button click event of the browser. <br>
     * 
     * The goBack command waits for the page to load after the navigation
     * 
     * @param waitTime
     *            : Time to wait for goBack command to complete
     * 
     * */
    @Override
    public final void goBack(final String waitTime) {

        try {
            RootTestObject root = script.getRootTestObject();
            TestObject[] browsers =
                    root.find(script.atDescendant(".class", "Html.HtmlBrowser"));
            if (browsers.length == 0) {
                throw new Exception("Cannot find any open test browser."
                        + waitTime + " seconds");
            }
            BrowserTestObject testBrowser = new BrowserTestObject(browsers[0]);
            testBrowser.back();
            Utils.pause(Integer.parseInt(waitTime));
            reportResults(ReportLogger.ReportLevel.SUCCESS, "Go Back",
                    "Success", "Browser go back successful.");

        } catch (Exception e) {
            reportResults(true, 
                    ReportLogger.ReportLevel.FAILURE,
                    "Go Back",
                    "Error",
                    "Cannot go back in the browser. ::: Actual Error : "
                            + e.getMessage());
        }

    }

    /**
     * Override.
     * 
     * @param objectName
     *            the object name
     * @return true, if successful
     * @see com.virtusa.isq.rft.runtime.ICommandBase#checkElementPresent(java.lang.String,
     *      boolean)
     */
    @Override
    public final int getObjectCount(final String objectName) {

        return getObjectCount(objectName, "");
    }

    /**
     * Override.
     * 
     * @param objectName
     *            the object name
     * @param identifier
     *            the identifier
     * @return true, if successful
     * @see com.virtusa.isq.rft.runtime.ICommandBase#checkElementPresent(java.lang.String,
     *      java.lang.String, boolean)
     */
    @Override
    public final int getObjectCount(final String objectName,
            final String identifier) {
        ObjectLocator locator =
                objectMap.getResolvedObjectSearchPath(objectName, identifier);
        return doGetObjectCount(locator);
    }

    /**
     * Do check element present.
     * 
     * @param locator
     *            the locator
     * @return true, if successful
     */
    private int doGetObjectCount(final ObjectLocator locator) {
        int objectCount = 0;
        try {
            TestObject[] objectIDArr = findElements(locator.getPropertyArray());
            objectCount = objectIDArr.length;
            reportResults(ReportLogger.ReportLevel.SUCCESS, "Get Object Count",
                    "Success", "Object count is " + objectCount);
        } catch (Exception e) {
            e.printStackTrace();
            reportResults(true, ReportLogger.ReportLevel.FAILURE, "Get Object Count",
                    "Error", "Cannot access the element. ::: " + "Object : "
                            + Arrays.asList(locator.getPropertyArray())
                            + " ::: " + "Actual Error : " + e.getMessage());

        } finally {
            script.unregisterAll();
        }
        return objectCount;
    }

    /**
     * Performs a Java robot click on the specific coordinates. <br>
     * 
     * @param resolution
     *            the resolution
     * @param coordinates
     *            the coordinates
     * @param waitTime
     *            the wait time
     * @throws Exception
     *             the exception
     */
    @Override
    public final void mouseMoveAndClick(final String resolution,
            final String coordinates, final String waitTime){

        String res = resolution;
        final int f11KeyCode = KeyEvent.VK_F11;
        final int optimumPauseBetweenkeyCombs = 10;
        String[] resArr = res.split(",");
        String[] coordinatesArr = coordinates.split(",");

        float screenWidht = Float.parseFloat(resArr[0]);
        float screeHigt = Float.parseFloat(resArr[1]);
        float xCordinate = Float.parseFloat(coordinatesArr[0]);
        float yCordinate = Float.parseFloat(coordinatesArr[1]);
        String command = "";

        if (coordinatesArr.length > 2) {

            command = coordinatesArr[2];
        }

        Robot robot = null;
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }

        Utils.pause(Integer.parseInt(waitTime));

        int xCordinateAutual = (int) calWidth(screenWidht, xCordinate);
        int yCordinateAutual = (int) calHight(screeHigt, yCordinate);

        robot.keyPress(f11KeyCode);
        robot.delay(optimumPauseBetweenkeyCombs);
        robot.keyRelease(f11KeyCode);
        Utils.pause(retryInterval);

        // Mouse Move
        robot.mouseMove(xCordinateAutual, yCordinateAutual);

        // Click
        if ("".equals(command)) {

            robot.mousePress(InputEvent.BUTTON1_MASK);
            Utils.pause(retryInterval);
            robot.mouseRelease(InputEvent.BUTTON1_MASK);

            reportResults(ReportLogger.ReportLevel.SUCCESS,
                    "Mouse Move And Click", "Success", "Resolution : " + res);

        } else if ("dclick".equals(command.toLowerCase(Locale.getDefault()))) {

            robot.mousePress(InputEvent.BUTTON1_MASK);
            robot.mouseRelease(InputEvent.BUTTON1_MASK);
            final int optimumPauseBetweenDclick = 500;
            robot.delay(optimumPauseBetweenDclick);
            robot.mousePress(InputEvent.BUTTON1_MASK);
            robot.mouseRelease(InputEvent.BUTTON1_MASK);

            reportResults(true, ReportLogger.ReportLevel.SUCCESS,
                    "Mouse Move And Click", "Success", "Resolution : " + res);

        }

        robot.keyPress(f11KeyCode);
        robot.delay(optimumPauseBetweenkeyCombs);
        robot.keyRelease(f11KeyCode);
    }

    public static enum TableValidationType {

        /** The colcount. */
        COLCOUNT,
        /** The rowcount. */
        ROWCOUNT,
        /** The tabledata. */
        TABLEDATA,
        /** The relative. */
        RELATIVE,
        /** The tablecell. */
        TABLECELL
    };

    @Override
    public final void checkTable(final String objectName,
            final String validationTypeS, final Object objExpectedvale,
            final boolean stopOnFaliure) {

        checkTable(objectName, "", validationTypeS, objExpectedvale,
                stopOnFaliure);
    }

    @Override
    public final void checkTable(final String objectName,
            final String identifier, final String validationTypeS,
            final Object objExpectedvale, final boolean stopOnFaliure) {
        ObjectLocator locator =
                objectMap.getResolvedObjectSearchPath(objectName, identifier);
        doCheckTable(locator, validationTypeS, objExpectedvale, stopOnFaliure);
    }

    private final void doCheckTable(final ObjectLocator locator,
            final String validationTypeS, final Object objExpectedvale,
            final boolean stopOnFaliure) {

        TableValidationType validationType =
                TableValidationType.valueOf(validationTypeS);

        try {
            TestObject[] objectIDArr = findElements(locator.getPropertyArray());
            if (objectIDArr.length > 0) {

                TestObject object = objectIDArr[0];
                if (validationType == TableValidationType.ROWCOUNT) {

                    validateTableRowCount(locator, object, objExpectedvale,
                            stopOnFaliure);
                } else if (validationType == TableValidationType.COLCOUNT) {

                    validateTableColCount(locator, object, objExpectedvale,
                            stopOnFaliure);
                } else if (validationType == TableValidationType.TABLEDATA) {

                    compareTableData(locator, object, objExpectedvale,
                            stopOnFaliure);
                } else if (validationType == TableValidationType.RELATIVE) {

                    validateTableOffset(locator, object, objExpectedvale,
                            stopOnFaliure);
                 } else if (validationType == TableValidationType.TABLECELL) {

                    validateCellValue(locator, object, objExpectedvale,
                            stopOnFaliure);
                 }
            } else {
                reportResults(stopOnFaliure, 
                        ReportLogger.ReportLevel.FAILURE,
                        "Check Table",
                        "Error",
                        "Cannot find the element. ::: "
                                + "Object : "
                                + locator.getLogicalName()
                                + " ::: "
                                + "Actual Error : Cannot find the element with properties : "
                                + Arrays.asList(locator.getPropertyArray()));

            }

        } catch (UserAbortedActionException ex) {
            throw ex;
        } catch (Exception ex) {
            ex.printStackTrace();
            reportResults(stopOnFaliure, ReportLogger.ReportLevel.FAILURE, "Check Table",
                    "Error", "Cannot access the element. ::: " + "Object : "
                            + Arrays.asList(locator.getPropertyArray())
                            + " ::: " + "Actual Error : " + ex.getMessage());
        } finally {
            script.unregisterAll();
        }

    }

    private void validateCellValue(ObjectLocator locator, TestObject object,
            Object objExpectedValue, boolean stopOnFaliure) {
        
        ArrayList<String> inputStringArray;
        boolean failedOnce = false;
        int row = -1;
        int col = -1;
        String cellText = "";
        String result = "";
        ArrayList<String> htmlTable = new ArrayList<String>();
        final int inputStringItems = 3;

        inputStringArray = new ArrayList<String>(Arrays.asList(objExpectedValue.toString()
                .split("(?<!\\\\),", Integer.MAX_VALUE)));

        ArrayList<String> tempInputTable = new ArrayList<String>();
        for (String inputVal : inputStringArray) {
            String formattedValue = inputVal.replaceAll("\\\\,", ",");
            tempInputTable.add(formattedValue);
        }
        inputStringArray = tempInputTable;

        if (inputStringArray.size() < inputStringItems) {
            failedOnce = true;
            reportResults(stopOnFaliure, ReportLogger.ReportLevel.FAILURE, "Check Table",
                    "Error", "Check table command TABLECELL failed. ::: " + "Object : "
                            + Arrays.asList(locator.getPropertyArray())
                            + " ::: " + "Actual Error : Verification data not provided correctly. "+ objExpectedValue );
            return;
        }
        row = Integer.parseInt(inputStringArray.get(0));
        col = Integer.parseInt(inputStringArray.get(1));

        cellText = StringUtils.join(
                inputStringArray.subList(2, inputStringArray.size()).toArray(),
                ",");

        try {
            
            htmlTable = getAppTableRow(object, row);
        } catch (Exception ex) {
            failedOnce = true;
            result = result + " | Expected Row : " + row
                    + " cannot be found in the actual table \n";
        }

        int verifyIndex = col; // get the sequential index of the value to be
                                // verified

        String verifyValue = "";

        try {
            verifyValue = htmlTable.get(verifyIndex).trim();

            if (!cellText.equals(verifyValue)) {
                failedOnce = true;
                result = result + " | Expected : " + cellText + " Actual :"
                        + htmlTable.get(verifyIndex) + "\n";

            }

        } catch (IndexOutOfBoundsException ex) {
            failedOnce = true;
            result = result + " | Expected Column : " + verifyIndex
                    + " cannot be found in the actual table \n";
        }

        if (failedOnce) {
            
            reportResults(stopOnFaliure, ReportLogger.ReportLevel.FAILURE, "Check Table",
                    "Error",
                    "Check Table TABLECELL command failed. ::: " + "Object : "
                            + Arrays.asList(locator.getPropertyArray())
                            + " ::: " + "Actual Error : Verification errors " + result);
            

        } else {
            reportResults(ReportLogger.ReportLevel.SUCCESS, "Check Table",
                    "Success", "Check table TABLECELL : " + objExpectedValue);
        }
        
    }

    private void validateTableOffset(ObjectLocator locator, TestObject object,
            Object objExpectedValue, boolean stopOnFaliure) {
        
        ArrayList<String> inputStringArray;
        String parentText = "";
        Integer offset;
        String cellText = "";
        String inputStringCurrStr = "";
        String result = "";
        ArrayList<String> htmlTable;

        htmlTable = getAppTable(object);
        StringBuilder resultBuilder = new StringBuilder();

        ArrayList<String> inputStringCurrArray;
        inputStringArray = new ArrayList<String>(Arrays.asList(objExpectedValue.toString()
                .split("#")));

        for (int i = 0; i < inputStringArray.size(); i++) {

            // Split the string to parts and entered to an array NAMED
            // inputTable

            // Getting the values out
            inputStringCurrStr = inputStringArray.get(i);

            inputStringCurrArray = new ArrayList<String>(
                    Arrays.asList(inputStringCurrStr.split("(?<!\\\\),",
                            Integer.MAX_VALUE)));

            ArrayList<String> tempInputTable = new ArrayList<String>();
            for (String inputVal : inputStringCurrArray) {
                String formattedValue = inputVal.replaceAll("\\\\,", ",");
                tempInputTable.add(formattedValue);
            }
            inputStringCurrArray = tempInputTable;

            parentText = inputStringCurrArray.get(0);
            offset = Integer.valueOf(inputStringCurrArray.get(1));
            cellText = inputStringCurrArray.get(2);
            resultBuilder
                    .append(checkIfTheTableContainsTheExpectedRelativeValue(
                            htmlTable, parentText, offset, cellText));
        }

        result = resultBuilder.toString();
        if (!result.isEmpty()) {
            
            reportResults(stopOnFaliure, ReportLogger.ReportLevel.FAILURE, "Check Table",
                    "Error",
                    "Check Table RELATIVE command failed. ::: " + "Object : "
                            + Arrays.asList(locator.getPropertyArray())
                            + " ::: " + "Actual Error : Verification errors " + result);
        } else {
            reportResults(ReportLogger.ReportLevel.SUCCESS, "Check Table",
                    "Success", "Check table relative : " + objExpectedValue);
        }
    
        
    }

    private String checkIfTheTableContainsTheExpectedRelativeValue(
            final List<String> htmlTable, final String parentText,
            final int offset, final String cellText) {
        int indexParent;
        StringBuilder resultBuilder = new StringBuilder();
        if (htmlTable.contains(parentText)) {

            ArrayList<Integer> parentTextIndexList = new ArrayList<Integer>();
            for (int k = 0; k < htmlTable.size(); k++) {
                if (htmlTable.get(k).equals(parentText)) {
                    parentTextIndexList.add(k);
                }
            }
            for (int j = 0; j < parentTextIndexList.size(); j++) {

                // indexParent = htmlTable.indexOf(parentText);
                indexParent = parentTextIndexList.get(j);
                String actualText = "";
                try {
                    actualText = htmlTable.get((indexParent + offset));
                    if (!cellText.equals(actualText)) {
                        // failedOnce = true;
                        resultBuilder.append(" | Expected : " + cellText
                                + " Actual :" + actualText + " Base value : " + parentText + "\n");
                    } else {
                        break;
                    }

                } catch (IndexOutOfBoundsException ex) {
                    resultBuilder
                            .append(" | Expected value : " + cellText
                                    + " cannot be found in the field: "
                                    + (indexParent + offset)
                                    + " in the actual table\n");
                }
            }
        } else {
            resultBuilder.append(" | Expected RELATIVE text: " + parentText
                    + " is not present in the actual table \n");
        }
        return resultBuilder.toString();
    }
    
    

    private void compareTableData(ObjectLocator locator, TestObject object,
            Object objExpectedValue, boolean stopOnFaliure) {

        ArrayList<String> htmlTable;
        ArrayList<String> inputTable;
        try {
            htmlTable = getAppTable(object);

            inputTable =
                    new ArrayList<String>(Arrays.asList(objExpectedValue
                            .toString().split("(?<!\\\\),")));
            ArrayList<String> tempInputTable = new ArrayList<String>();
            for (String inputVal : inputTable) {
                String formattedValue = inputVal.replaceAll("\\\\,", ",");
                tempInputTable.add(formattedValue);
            }
            inputTable = tempInputTable;

            String inputTableStr = StringUtils.join(inputTable, "|");
            String actualTableStr = StringUtils.join(htmlTable, "|");

            if (actualTableStr.contains(inputTableStr)) {

                reportResults(ReportLogger.ReportLevel.SUCCESS, "Check Table",
                        "Success", "Check table data : " + objExpectedValue);

            } else {
                String inputTableString = inputTable.toString();
                String htmlTableString = htmlTable.toString();
                reportResults(stopOnFaliure, ReportLogger.ReportLevel.FAILURE, "Check Table",
                        "Error",
                        "Check Table TABLEDATA command failed. ::: " + "Object : "
                                + Arrays.asList(locator.getPropertyArray())
                                + " ::: " + "Actual Error : Expected data "+inputTableString+"  does not match the actual : "+htmlTableString);
            }

        } catch (UserAbortedActionException ex) {
            throw ex;
        } catch (Exception ex) {
            reportResults(stopOnFaliure, ReportLogger.ReportLevel.FAILURE, "Check Table",
                    "Error", "Cannot access the element. ::: " + "Object : "
                            + Arrays.asList(locator.getPropertyArray())
                            + " ::: " + "Actual Error : " + ex.getMessage());
        }        

    }

    private ArrayList<String> getAppTable(TestObject object) {

        ArrayList<String> htmlTable = new ArrayList<String>();

        StatelessGuiSubitemTestObject html_table =
                (StatelessGuiSubitemTestObject) object;
        // table parsing
        ITestDataTable table = (ITestDataTable) html_table.getTestData("grid");
        int nbRows = table.getRowCount();
        int nbCols = table.getColumnCount();

        for (int row = 0; row < nbRows; row++) {
            for (int col = 0; col < nbCols; col++) {
                Object cell = table.getCell(row, col);
                if (table.getCell(row, col) != null) {
                    
                    htmlTable.add(cell.toString());
                } else {
                    htmlTable.add("");
                }
            }
        }
        return htmlTable;
    }
    
    private ArrayList<String> getAppTableRow(TestObject object, int row) {

        ArrayList<String> htmlTable = new ArrayList<String>();

        StatelessGuiSubitemTestObject html_table =
                (StatelessGuiSubitemTestObject) object;
        // table parsing
        ITestDataTable table = (ITestDataTable) html_table.getTestData("grid");
        int nbCols = table.getColumnCount();

        for (int col = 0; col < nbCols; col++) {
            Object cell = table.getCell(row, col);
            if (table.getCell(row, col) != null) {

                htmlTable.add(cell.toString());
            } else {
                htmlTable.add("");
            }
        }
        return htmlTable;
    }

    private void validateTableColCount(ObjectLocator locator,
            TestObject object, Object objExpectedvale, boolean stopOnFaliure) {

        try {
            StatelessGuiSubitemTestObject html_table =
                    (StatelessGuiSubitemTestObject) object;
            // table parsing
            ITestDataTable table =
                    (ITestDataTable) html_table.getTestData("grid");
            int count = table.getColumnCount();
            if (count == Integer.parseInt(objExpectedvale.toString())) {
                reportResults(ReportLogger.ReportLevel.SUCCESS, "Check Table",
                        "Success", "Check column count : " + objExpectedvale);
            } else {
                reportResults(stopOnFaliure,
                        ReportLogger.ReportLevel.FAILURE,
                        "Check Table",
                        "Error",
                        "Check Table COLCOUNT command failed. ::: "
                                + "Object : "
                                + Arrays.asList(locator.getPropertyArray())
                                + " ::: "
                                + "Actual Error : Expected column count "
                                + objExpectedvale
                                + " does not match the actual " + count + "");
            }

        } catch (UserAbortedActionException ex) {
            throw ex;
        } catch (NumberFormatException ex) {

            ex.printStackTrace();
            reportResults(stopOnFaliure, ReportLogger.ReportLevel.FAILURE, "Check Table",
                    "Error",
                    "Check Table COLCOUNT command failed. Invalid input data : "
                            + objExpectedvale + ". ::: " + "Object : "
                            + Arrays.asList(locator.getPropertyArray())
                            + " ::: " + "Actual Error : " + ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            reportResults(stopOnFaliure,
                    ReportLogger.ReportLevel.FAILURE,
                    "Check Table",
                    "Error",
                    "Check Table COLCOUNT command failed. Cannot access the element. ::: "
                            + "Object : "
                            + Arrays.asList(locator.getPropertyArray())
                            + " ::: " + "Actual Error : " + ex.getMessage());
        }

    }

    private void validateTableRowCount(ObjectLocator locator,
            TestObject object, Object objExpectedvale, boolean stopOnFaliure) {

        try {
            StatelessGuiSubitemTestObject html_table =
                    (StatelessGuiSubitemTestObject) object;
            // table parsing
            ITestDataTable table =
                    (ITestDataTable) html_table.getTestData("grid");
            int count = table.getRowCount();
            if (count == Integer.parseInt(objExpectedvale.toString())) {
                reportResults(ReportLogger.ReportLevel.SUCCESS, "Check Table",
                        "Success", "Check row count : " + objExpectedvale);
            } else {
                reportResults(stopOnFaliure,
                        ReportLogger.ReportLevel.FAILURE,
                        "Check Table",
                        "Error",
                        "Check Table ROWCOUNT command failed. Check Table command failed. ::: "
                                + "Object : "
                                + Arrays.asList(locator.getPropertyArray())
                                + " ::: "
                                + "Actual Error : Expected row count "
                                + objExpectedvale
                                + " does not match the actual " + count + "");
            }

        } catch (UserAbortedActionException ex) {
            throw ex;
        } catch (NumberFormatException ex) {

            ex.printStackTrace();
            reportResults(stopOnFaliure, ReportLogger.ReportLevel.FAILURE, "Check Table",
                    "Error",
                    "Check Table ROWCOUNT command failed. Invalid input data : "
                            + objExpectedvale + ". ::: " + "Object : "
                            + Arrays.asList(locator.getPropertyArray())
                            + " ::: " + "Actual Error : " + ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            reportResults(stopOnFaliure,
                    ReportLogger.ReportLevel.FAILURE,
                    "Check Table",
                    "Error",
                    "Check Table ROWCOUNT command failed. Cannot access the element. ::: "
                            + "Object : "
                            + Arrays.asList(locator.getPropertyArray())
                            + " ::: " + "Actual Error : " + ex.getMessage());
        }

    }

}
