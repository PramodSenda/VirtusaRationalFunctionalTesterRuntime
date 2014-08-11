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

package com.virtusa.isq.rft.objectmap;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.rational.test.ft.script.Property;

/**
 * The Class ObjectMapParser.
 */
public class ObjectMapParser implements IGetObjectMap {

    /**
     * Override ObjectLocator.
     * 
     * @param unresolvedObject
     *            the unresolved object
     * @param identifier
     *            the identifier
     * @return the resolved object search path
     * @see com.virtusa.isq.rft.objectmap.IGetObjectMap#getResolvedObjectSearchPath(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public final ObjectLocator getResolvedObjectSearchPath(
            final String unresolvedObject, final String identifier) {

        ObjectLocator locator = new ObjectLocator(unresolvedObject, identifier);
        String locatorString = getActualLocatorString(unresolvedObject);
        Property[] resolvedPropertyArray;

        if ("".equals(identifier)) {
            resolvedPropertyArray = getObjectPropertyArray(locatorString);

        } else {
            String resolvedLocatorString =
                    getResolvedSearchPath(locatorString, identifier);
            resolvedPropertyArray =
                    getObjectPropertyArray(resolvedLocatorString);

        }
        locator.setPropertyArray(resolvedPropertyArray);
        return locator;
    }

    /**
     * Creates the property array based on the object locator String.
     * 
     * @param resolvedObjectLocatorStr
     *            the resolved object locator str
     * @return created Property array
     */
    private Property[] getObjectPropertyArray(
            final String resolvedObjectLocatorStr) {
        Property[] resolvedPropertyArray = null;
        String locatorStr = resolvedObjectLocatorStr;
        List<String> attributes = new ArrayList<String>();
        ArrayList<Property> propertyList = new ArrayList<Property>();
        try {
            if (locatorStr.contains("|")) {
                attributes = Arrays.asList(locatorStr.split("\\|"));
            } else {
                attributes.add(locatorStr);
            }
            for (String attribute : attributes) {
                try {
                    int arrkey = 0;
                    int arrval = 1;
                    Property property = null;
                    
                    if (attribute.contains(":=")) {
                        String[] attributeKeyValue =
                                attribute.split(":=", Integer.MAX_VALUE);
                        property =
                                new Property(attributeKeyValue[arrkey],
                                        attributeKeyValue[arrval]);
                    }else {
                        property =  new Property("", "");
                    }
                   propertyList.add(property);
                } catch (Exception e) {
                  e.printStackTrace();
                }
            }
            resolvedPropertyArray =
                    propertyList.toArray(new Property[propertyList.size()]);

        } catch (Exception e) {
           // e.printStackTrace();
        }

        return resolvedPropertyArray;
    }


    /**
     * Gets the actual locator string.
     * 
     * @param objectName
     *            the object name
     * @return the actual locator string
     */
    private String getActualLocatorString(final String objectName) {
        String actualLocatorPath = null;
        FileInputStream pageFileStream = null;
        try {
            String[] objectDataArr = objectName.split("\\.");
            String page = objectDataArr[0];
            String object = objectDataArr[1];

            pageFileStream =
                    new FileInputStream("Pages" + File.separator + "" + page
                            + ".properties");

            java.util.Properties propPage = new java.util.Properties();
            propPage.load(pageFileStream);
            actualLocatorPath = propPage.get(object).toString();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (pageFileStream != null) {
                try {
                    pageFileStream.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return actualLocatorPath;
    }

    /**
     * Gets the parameter values.
     * 
     * @param parameters
     *            the parameters
     * @return the parameter values
     */
    public static List<String> getParameterValues(final String parameters) {

        List<String> parameterValues = new ArrayList<String>();
        String[] st = parameters.split("_PARAM,");

        for (int i = 0; i < st.length; i++) {

            parameterValues.add(st[i]);

        }
        return parameterValues;

    }

    /**
     * Gets the resolved search path.
     * 
     * @param searchPath
     *            the search path
     * @param identifire
     *            the identifire
     * @return the resolved search path
     */
    public static String getResolvedSearchPath(final String searchPath,
            final String identifire) {
        String resolvedSearchPath = searchPath;
        if (!"".equals(identifire)) {
            List<String> parameterValues = getParameterValues(identifire);
            int splitindex0 = 0;
            int splitindex1 = 1;
            for (int i = 0; i < parameterValues.size(); i++) {
                resolvedSearchPath =
                        resolvedSearchPath
                                .replace(
                                        "<"
                                                + parameterValues.get(i).split(
                                                        "_PARAM:")[splitindex0]
                                                + ">", parameterValues.get(i)
                                                .split("_PARAM:")[splitindex1]);
            }
        }
        return resolvedSearchPath;
    }

    /*public static Property[] getResolvedSearchPath(Property[] searchPath,
            final String identifire) {
        Property[] resolvedSearchPath = new Property[searchPath.length];
        List<String> parameterValues = getParameterValues(identifire);
        for (int i = 0; i < searchPath.length; i++) {
            boolean isIdentifierFound = false;
            Property currProp = searchPath[i];
            for (int j = 0; j < parameterValues.size(); j++) {
                if (currProp
                        .getPropertyValue()
                        .toString()
                        .contains(
                                "<"
                                        + parameterValues.get(j).split(
                                                "_PARAM:")[0] + ">")) {
                    String resolvedPropertyValue =
                            currProp.getPropertyValue()
                                    .toString()
                                    .replace(
                                            "<"
                                                    + parameterValues.get(j)
                                                            .split("_PARAM:")[0]
                                                    + ">",
                                            parameterValues.get(j).split(
                                                    "_PARAM:")[1]);
                    resolvedSearchPath[i] =
                            new Property(currProp.getPropertyName(),
                                    resolvedPropertyValue);
                    isIdentifierFound = true;
                    break;
                }
            }
            if (!isIdentifierFound) {
                resolvedSearchPath[i] = currProp;
            }
        }
        return resolvedSearchPath;
    }
    */
}
