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

import com.rational.test.ft.script.Property;

/**
 * The Class ObjectLocator.
 */
public class ObjectLocator {

    /** The logical name. */
    private String logicalName;

    /** The identifier. */
    private String identifier = null;

    /** The property array. */
    private Property[] propertyArray = null;

    /**
     * Instantiates a new object locator.
     * 
     * @param logicalStrName
     *            the logical name
     */
    public ObjectLocator(final String logicalStrName) {
        this.logicalName = logicalStrName;
    }

    /**
     * Instantiates a new object locator.
     * 
     * @param logicalStrName
     *            the logical Str name
     * @param identifierObject
     *            the identifier Object
     */
    public ObjectLocator(final String logicalStrName,
            final String identifierObject) {
        this.logicalName = logicalStrName;
        this.identifier = identifierObject;
    }

    /**
     * Gets the logical name.
     * 
     * @return the logical name
     */
    public final String getLogicalName() {
        return logicalName;
    }

    /**
     * Gets the identifier.
     * 
     * @return the identifier
     */
    public final String getIdentifier() {
        return identifier;
    }

    /**
     * Sets the identifier.
     * 
     * @param strIdentifier
     *            the new Str Identifier
     */
    public final void setIdentifier(final String strIdentifier) {
        this.identifier = strIdentifier;
    }

    /**
     * Gets the property array.
     * 
     * @return the property array
     */
    public final Property[] getPropertyArray() {
        return propertyArray.clone();
    }

    /**
     * Sets the property array.
     * 
     * @param newPropertyArray
     *            the new property array
     */
    public final void setPropertyArray(final Property[] newPropertyArray) {
        this.propertyArray = newPropertyArray.clone();
    }

}
