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

package com.virtusa.isq.rft.scripthelper;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.rational.test.ft.object.interfaces.BrowserTestObject;
import com.rational.test.ft.object.interfaces.RootTestObject;
import com.rational.test.ft.object.interfaces.TestObject;
import com.rational.test.ft.script.RationalTestScript;
import com.rational.test.ft.script.SubitemFactory;
import com.virtusa.isq.rft.utils.PropertyHandler;
import com.virtusa.isq.rft.utils.Utils;

/**
 * The Class RFTScriptHelper.
 */
public class RFTScriptHelper extends RationalTestScript {

    /** The script. */
    private RationalTestScript script;

    /**
     * Instantiates a new RFT script helper.
     * 
     * @param testScript
     *            the Test Script
     */
    public RFTScriptHelper(final RationalTestScript testScript) {
        this.script = testScript;
        setup();
    }

    /**
     * Tear down.
     */
    public final void tearDown() {
        cleanBrowserSession();
        PropertyHandler propfile = new PropertyHandler("RUNTIME.properties");
        String browser = propfile.getRuntimeProperty("BROWSER");

        killAppProcess(browser);
        script.cleanup();
        unregisterAll();
    }

    /**
     * Clean browser session for the test execution.
     */
    private void cleanBrowserSession() {
        try {
            RootTestObject root = RationalTestScript.getRootTestObject();
            TestObject[] browsers =
                    root.find(SubitemFactory.atDescendant(".class",
                            "Html.HtmlBrowser"));
            if (browsers.length == 0) {
                throw new Exception("No browser found");
            }
            BrowserTestObject testBrowser = new BrowserTestObject(browsers[0]);
            testBrowser.deleteCookies();
            String command =
                    "RunDll32.exe InetCpl.cpl,ClearMyTracksByProcess 255";
            Runtime.getRuntime().exec(command).waitFor();

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * Kill app process.
     * 
     * @param process
     *            the process
     */
    private void killAppProcess(final String process) {

        if ("Internet Explorer".equalsIgnoreCase(process)) {
            killBrowserProcess("iexplore");
        }

    }

    /**
     * Kill the browser process.<br>
     * Specify the browser process to be killed.
     * 
     * @param process
     *            the process
     * @Parameters<br> process name which should be killed. <br>
     *                 Ex:<br>
     *                 If the process is firefox.exe parameter should be firefox
     */
    public final void killBrowserProcess(final String process) {
        String processName = process + ".exe";
        final int timeToRecoverFromProcessKill = 3000;
         try {
            if (isProcessRunning(processName)) {
                this.killProcess(processName);
                System.out.println("INFO : " + process
                        + " application session cleaned successfully");
                Utils.pause(timeToRecoverFromProcessKill);
            }
        } catch (Exception ex) {
            System.out.println("INFO : " + process
                    + " application session clean failed");
        }
    }

    /**
     * Checks if is process running.
     * 
     * @param serviceName
     *            the service name
     * @return true, if is process running
     * @throws Exception
     *             the exception
     */
    private boolean isProcessRunning(final String serviceName) throws Exception {
        String taskList = "tasklist";
        Process p = Runtime.getRuntime().exec(taskList);
        BufferedReader reader = null;
        try {
            reader =
                    new BufferedReader(
                            new InputStreamReader(p.getInputStream()));
            String line;

            while ((line = reader.readLine()) != null) {
                // System.out.println(line);
                if (line.contains(serviceName)) {
                    return true;
                }
            }
            return false;
        } finally {
            if (reader != null) {
                reader.close();

            }
        }

    }

    /**
     * Kill process.
     * 
     * @param serviceName
     *            the service name
     * @throws Exception
     *             the exception
     */
    private void killProcess(final String serviceName) throws Exception {
        String kill = "taskkill /F /IM ";
        Runtime.getRuntime().exec(kill + serviceName);
    }

    /**
     * Setup.
     */
    public final void setup() {
        PropertyHandler propfile = new PropertyHandler("RUNTIME.properties");
        String browser = propfile.getRuntimeProperty("BROWSER");
        killAppProcess(browser);
    }
    
   

}
