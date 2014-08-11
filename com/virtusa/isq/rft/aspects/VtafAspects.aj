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

import java.lang.reflect.Method;
import java.util.Arrays;

public aspect VtafAspects {

    /*pointcut beforeExecution(): (execution(void Launcher.testMain(..))); 
    before():beforeExecution(){
        System.out.println("-Before Execution-");
    }*/

    // pointcut afterMethod(): ((execution(void *.testMain(..)) ) &&
    // !within(Launcher));
    // pointcut afterMethod(): (execution
    // (@com.virtusa.isq.rft.annotations.VTAFAfterMethod * *.*(..)));
    /*pointcut afterMethod(): (execution (@com.virtusa.isq.rft.annotations.VTAFAfterMethod * *.*(..)));
    after():afterMethod(){
        
        System.out.println("-After Method-");
        
    }*/

    /*@After("@annotation(VTAFTestMethod)")
    public void afterMethod(){
        
        System.out.println("-After Method-");
        
    }
    
    @Before("@annotation(VTAFTestMethod)")
    public void beforeMethod(){
        
        System.out.println("-Before Method-");
       
    }*/

    /*pointcut beforeCallScript():  (execution (@com.virtusa.isq.rft.annotations.VTAFBeforeMethod * *.*(..))); 
    Object around():beforeCallScript(){
        //System.out.println("-Before Method-");
        proceed();
        return null;
    }*/
    pointcut callScriptExceptionHandler() : (call(* Launcher.callScript(..)));

    Object around():callScriptExceptionHandler(){
        Object ret = null;
        try {
            ret = proceed();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return ret;
    }

    /*pointcut testMainCall() : (execution(@com.virtusa.isq.rft.annotations.VTAFTestMethod * *.*(..))); 
    //pointcut testMainCall(): ((execution(void *.testMain(..)) ) && !within(Launcher)); 
    before():testMainCall(){

        System.out.println("------------------- BEFOREEEE --------------------");
        System.out.printf("Enters on method: %s. \n", thisJoinPoint.getSignature());
        System.out.println("------------------- END --------------------");
    }*/

    pointcut dataDrivenCall() : (execution(@com.virtusa.isq.rft.annotations.VTAFTestMethod * *.*(..)));

    Object around():dataDrivenCall(){

        int dataRowCount = 1;
        Object[] args = thisJoinPoint.getArgs();
        if (args.length > 0) {
            System.out.println("Initial dataRowCount : " + dataRowCount);
            try {
                Object[] dataDrivenArgs = (Object[]) args[0];
                if (dataDrivenArgs.length > 0) {
                    System.out.println(Arrays.asList(dataDrivenArgs));
                    // System.out.println("dataDrivenArgs lengh : "+dataDrivenArgs.length);
                    dataRowCount =
                            Integer.parseInt(dataDrivenArgs[dataRowCount].toString());
                    System.out.println("dataRowCount : " + dataRowCount);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // System.out.println("No of argument = " + args.length);
        Object ret = null;

        for (int i = 0; i < dataRowCount; i++) {

            ret = null;
            try {
                if (dataRowCount > 1) {
                    System.out.println("No of cycles = " + dataRowCount);
                    invokeNewDataDrivenReportLogger(thisJoinPoint
                            .getSignature().getDeclaringTypeName());
                }
                ret = proceed();
                // CALL AFTER METHOD...
            } catch (Exception e) {
                System.err.println("ERROR OCCURED: " + e.getMessage());
                // e.printStackTrace();
            }
        }
        return ret;
    }

    private void invokeNewDataDrivenReportLogger(final Object tcName) {
        System.out.println("Invoking test case : " + tcName);
    }
}
