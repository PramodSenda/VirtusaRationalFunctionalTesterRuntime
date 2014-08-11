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

/**
 * The Interface ICommandBase.
 */
public interface ICommandBase {

    /**
     * Open.
     * 
     * @param url
     *            the url
     * @param waitTime
     *            the wait time
     */
    void open(String url, String waitTime);
    
    /**
     * Open.
     * 
     * @param url
     *            the url
     * @param identifier
     *            identifier value
     * @param waitTime
     *            the wait time
     */
    void open(String url, String identifier, String waitTime);

    /**
     * Navigate to url.
     * 
     * @param url
     *            the url
     * @param waitTime
     *            the wait time
     *  
     */
    void navigateToURL(String url, String waitTime);

    /**
     * Click.
     * 
     * @param objectName
     *            the object name
     */
    void click(String objectName);

    /**
     * Click.
     * 
     * @param objectName
     *            the object name
     * @param identifier
     *            the identifier
     */
    void click(String objectName, String identifier);

    /**
     * Type.
     * 
     * @param objectName
     *            the object name
     * @param inputValue
     *            the input value
     */
    void type(String objectName, Object inputValue);

    /**
     * Type.
     * 
     * @param objectName
     *            the object name
     * @param identifier
     *            the identifier
     * @param inputValue
     *            the input value
     */
    void type(String objectName, String identifier, Object inputValue);

    /**
     * Pause.
     * 
     * @param waitTime
     *            the wait time
     */
    void pause(String waitTime);

    /**
     * Check element present.
     * 
     * @param objectName
     *            the object name
     * @param stopOnFailure
     *            the stop on failure
     * @return true, if successful
     */
    boolean checkElementPresent(String objectName, boolean stopOnFailure);

    /**
     * Check element present.
     * 
     * @param objectName
     *            the object name
     * @param identifier
     *            the identifier
     * @return true, if successful
     */
    boolean checkElementPresent(String objectName, String identifier);

    /**
     * Check element present.
     * 
     * @param objectName
     *            the object name
     * @param identifier
     *            the identifier
     * @param stopOnFailure
     *            the stop on failure
     * @return true, if successful
     */
    boolean checkElementPresent(String objectName, String identifier,
            boolean stopOnFailure);

    /**
     * Select.
     * 
     * @param objectName
     *            the object name
     * @param option
     *            the option
     */
    void select(String objectName, String option);

    /**
     * Select.
     * 
     * @param objectName
     *            the object name
     * @param identifier
     *            the identifier
     * @param option
     *            the option
     */
    void select(String objectName, String identifier, String option);

    /**
     * Check object property.
     * 
     * @param objectName
     *            the object name
     * @param propertyName
     *            the property name
     * @param expectedValue
     *            the expected value
     * @param stopOnFailure
     *            the stop on failure
     */
    void checkObjectProperty(String objectName, String propertyName,
            String expectedValue, boolean stopOnFailure);

    /**
     * Check object property.
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
     */
    void checkObjectProperty(String objectName, String identifier,
            String propertyName, String expectedValue, boolean stopOnFailure);

    /**
     * Check pattern.
     * 
     * @param objectName
     *            the object name
     * @param identifier
     *            the identifier
     * @param pattern
     *            the pattern
     */
    void checkPattern(String objectName, String identifier, String pattern);

    /**
     * Check pattern.
     * 
     * @param objectName
     *            the object name
     * @param pattern
     *            the pattern
     */
    void checkPattern(String objectName, String pattern);

    /**
     * Click at.
     *
     * @param objectName the object name
     * @param identifier the identifier
     * @param coordinates the coordinates
     */
    void clickAt(String objectName, String identifier, String coordinates);

    /**
     * Click at.
     *
     * @param objectName the object name
     * @param coordinates the coordinates
     */
    void clickAt(String objectName, String coordinates);

    /**
     * Store.
     *
     * @param key the key
     * @param type the type
     * @param value the value
     */
    void store(String key, String type, String value);

    /**
     * Retrieve float.
     *
     * @param key the key
     * @return the float
     */
    float retrieveFloat(String key);

    /**
     * Retrieve string.
     *
     * @param key the key
     * @return the string
     */
    String retrieveString(String key);

    /**
     * Retrieve int.
     *
     * @param key the key
     * @return the int
     */
    int retrieveInt(String key);

    /**
     * Retrieve boolean.
     *
     * @param key the key
     * @return true, if successful
     */
    boolean retrieveBoolean(String key);

    /**
     * Double click at.
     *
     * @param objectName the object name
     * @param coordinates the coordinates
     */
    void doubleClickAt(String objectName, String coordinates);

    /**
     * Double click at.
     *
     * @param objectName the object name
     * @param identifier the identifier
     * @param coordinates the coordinates
     */
    void doubleClickAt(String objectName, String identifier, String coordinates);

    /**
     * Double click.
     *
     * @param objectName the object name
     */
    void doubleClick(String objectName);

    /**
     * Double click.
     *
     * @param objectName the object name
     * @param identifier the identifier
     */
    void doubleClick(String objectName, String identifier);

    /**
     * Fire event.
     *
     * @param event the event
     * @param waittime the waittime
     */
    void fireEvent(String event, String waittime);

    /**
     * Fail.
     *
     * @param message the message
     */
    void fail(Object message);

    /**
     * Key press.
     *
     * @param objectName the object name
     * @param identifier the identifier
     * @param inputValue the input value
     */
    void keyPress(String objectName, String identifier, Object inputValue);

    /**
     * Key press.
     *
     * @param objectName the object name
     * @param inputValue the input value
     */
    void keyPress(String objectName, Object inputValue);

    /**
     * Mouse over.
     *
     * @param objectName the object name
     * @param identifier the identifier
     */
    void mouseOver(String objectName, String identifier);

    /**
     * Mouse over.
     *
     * @param objectName the object name
     */
    void mouseOver(String objectName);

    /**
     * Go back.
     *
     * @param waitTime the wait time
     */
    void goBack(String waitTime);

    /**
     * Gets the object count.
     *
     * @param objectName the object name
     * @param identifier the identifier
     * @return the object count
     */
    int getObjectCount(String objectName, String identifier);

    /**
     * Gets the object count.
     *
     * @param objectName the object name
     * @return the object count
     */
    int getObjectCount(String objectName);

    void mouseMoveAndClick(String resolution, String coordinates, String waitTime) throws Exception;

    void checkTable(String objectName, String identifier,
            String validationTypeS, Object objExpectedvale,
            boolean stopOnFaliure);


    void checkTable(String objectName, String validationTypeS,
            Object objExpectedvale, boolean stopOnFaliure);

    void navigateToURL(String url, String identifier, String waitTime);

}
