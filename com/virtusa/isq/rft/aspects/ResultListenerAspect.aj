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

package com.virtusa.isq.rft.aspects;

import java.util.Arrays;

import com.rational.test.ft.script.RationalTestScript;
import com.virtusa.isq.vtaf.report.reporter.Reporter;

public aspect ResultListenerAspect {

    private Reporter reporter = null;
    private String currTCName = "";
    private String currTSName = "";

    pointcut initReport() : (execution(void Launcher.testMain(..)));

    before():initReport(){
        reporter = new Reporter();
        reporter.addNewTestExecution();
    }

    pointcut endTestReport() : (execution(void Launcher.testMain(..)));

    after():endTestReport(){
        reporter.endTestReporting();
    }

    pointcut dataDrivenTestCaseListener() : (call(void *.invokeNewDataDrivenReportLogger(..)));

    before():dataDrivenTestCaseListener(){
        String testCaseName = "";
        String testSuiteName = "";
        Object[] args = thisJoinPoint.getArgs();
       
        try {
            int packageIndex = 0;
          
            String scriptName = args[packageIndex].toString();
            if (scriptName.contains(".")) {
                int classIndex = 2;
                int caseIndex = 3;
                testCaseName = scriptName.split("\\.", Integer.MAX_VALUE)[caseIndex];
                testSuiteName = scriptName.split("\\.", Integer.MAX_VALUE)[classIndex];
            }
            if (!testSuiteName.equalsIgnoreCase(currTSName)) {
                reporter.addNewTestSuite(testSuiteName);
                currTSName = testSuiteName;
            }
            reporter.addNewTestCase(testCaseName);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // reporter.addNewTestCase(currTCName);
        // System.out.println("Added a new test case : " + currTCName);
    }

    pointcut resultListener() : (call(* com.virtusa.isq.rft.runtime.ReportLogger.logResult(..)));

    before():resultListener(){
        try {
            int arg0 = 0;
            int arg1 = 1;
            int arg2 = 2;
            int arg3 = 3;
            int arg4 = 4;
            Object[] args = thisJoinPoint.getArgs();
            RationalTestScript script = (RationalTestScript) args[arg0];
            String level = args[arg1].toString();
            String step = args[arg2].toString();
            String result = args[arg3].toString();
            String message = args[arg4].toString();

            message = replaceXMLSpecialCharacters(message);

            checkNewTestCase(script);

            try {
                if ("SUCCESS".equals(level)) {
                    reporter.reportStepResults(true, step, message, result, "");
                } else {
                    reporter.reportStepResults(false, step, message, result,
                            getSourceLines(new Throwable(message)
                                    .getStackTrace()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkTestCaseNameAndAddTestCases(final String scriptName,
            final boolean isDatadriven) {
        String testCaseName = "";
        String testSuiteName = "";

        if (scriptName.contains(".")) {
        int tcindex = 3;
        int tsindex = 2;
            testCaseName = scriptName.split("\\.", Integer.MAX_VALUE)[tcindex];
            testSuiteName = scriptName.split("\\.", Integer.MAX_VALUE)[tsindex];
        }

        if (!testSuiteName.equalsIgnoreCase(currTSName)) {

            System.out.println("OLD TS : " + currTSName);

            currTSName = testSuiteName;
            reporter.addNewTestSuite(currTSName);
            System.out.println("#### Added a new test Suite : " + currTSName);
        }
        if (!testCaseName.equalsIgnoreCase(currTCName) && !isDatadriven) {

            System.out.println("OLD TC : " + currTCName);
            System.out.println("NEW TC : " + testCaseName);

            currTCName = testCaseName;
            reporter.addNewTestCase(currTCName);
            System.out.println("Added a new test Case : " + currTCName);
        }
    }

    private void checkNewTestCase(final RationalTestScript script) {
        boolean isDataDrivenScript = false;
        try {
            String scriptName = script.getScriptName();
            Object[] args = script.getScriptArgs();
            System.out.println("ARGS : " + Arrays.asList(args) + " Length = "
                    + args.length);
            // Object[] dataDrivenArgs = (Object[])args[0];
            int arg1= 1;
            int dataRowCount = Integer.parseInt(args[arg1].toString());
            if (dataRowCount > 1) {
                isDataDrivenScript = true;
            }
            System.out.println("Test Script Name = " + scriptName);

            checkTestCaseNameAndAddTestCases(scriptName, isDataDrivenScript);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getSourceLines(final StackTraceElement[] StackTrace) {
        String lines = "";
        StringBuilder sb = new StringBuilder();
        for (int elementid = 0; elementid < StackTrace.length; elementid++) {
            if (StackTrace[elementid].toString().indexOf("invoke0") != -1) {
                
                sb.append(StackTrace[elementid - 1] );
                sb.append("|");
                sb.append(StackTrace[elementid - 2] );
                sb.append("|");
                sb.append(StackTrace[elementid - 3] );
                
               /* lines =
                        lines + StackTrace[elementid - 1] + "|"
                                + StackTrace[elementid - 2] + "|"
                                + StackTrace[elementid - 3];*/
            }
            lines = sb.toString();
        }
        return lines;
    }

    public String replaceXMLSpecialCharacters(final String text) {
        String replaced = text;
/*
        replaced = replaced.replaceAll("<", "&lt;");
        replaced = replaced.replaceAll(">", "&gt;");
        replaced = replaced.replaceAll("&", "&amp;");
        replaced = replaced.replaceAll("'", "&apos;");
        replaced = replaced.replaceAll("\"", "&quot;");
        replaced = replaced.replaceAll("»", "");*/
        return replaced.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("&", "&amp;").replaceAll("'", "&apos;").replaceAll("\"", "&quot;").replaceAll("»", "");
    }

}
