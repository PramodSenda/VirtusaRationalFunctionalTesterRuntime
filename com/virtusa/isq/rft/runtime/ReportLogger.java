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

import com.rational.test.ft.script.RationalTestScript;

/**
 * The Class ReportLogger.
 */
public class ReportLogger extends RationalTestScript {

    /**
     * The Enum ReportLevel.
     */
    public static enum ReportLevel {

        /** The success. */
        SUCCESS,
        /** The failure. */
        FAILURE,
        /** The verification failure. */
        VERIFICATION_FAILURE
    };

    /**
     * Log result.
     * 
     * @param script
     *            the script
     * @param level
     *            the level
     * @param step
     *            the step
     * @param result
     *            the result
     * @param message
     *            the message
     */
    public final void logResult(final RationalTestScript script,
            final ReportLogger.ReportLevel level, final String step,
            final String result, final String message) {
        logInfo("Step : " + step + "\tResult : " + result + "\tMessage : "
                + message);
    }
}
