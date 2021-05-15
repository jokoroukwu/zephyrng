package com.gmail.suneclips3;

import com.gmail.suneclips3.annotations.ZephyrStep;
import org.testng.*;
import org.testng.xml.XmlSuite;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class MyReporter implements IReporter {

    @Override
    public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
        for (ISuite suite : suites) {
            for (ISuiteResult suiteResult : suite.getResults().values()) {
                final ITestContext testContext = suiteResult.getTestContext();
                Stream.of(testContext.getFailedTests().getAllResults(),
                        testContext.getFailedConfigurations().getAllResults())
                        .flatMap(Set::stream)
                        .map(testResult -> {
                            try {
                                return findStep(testResult);
                            } catch (ClassNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                        }).forEach(step -> System.out.printf("FOUND FINAL STEP: '%s'\n", step));
            }
        }
    }

    private String findStep(ITestResult testResult) throws ClassNotFoundException {
        final Class<?> testClass = testResult.getTestClass().getRealClass();
        final StackTraceElement[] stackTraceElements = testResult.getThrowable().getStackTrace();
        String stepName = null;
        for (int i = 0, k = stackTraceElements.length; i < k; i++) {
            StackTraceElement stackTraceElement = stackTraceElements[i];
            Class<?> nextClass = Class.forName(stackTraceElement.getClassName());

            ZephyrStep zephyrStep = scanClassMethodsForStep(nextClass, stackTraceElement.getMethodName());
            if (zephyrStep != null) {
                stepName = zephyrStep.description();
            }
            if (testClass.equals(nextClass) || nextClass.isAssignableFrom(testClass)) {
                //  искать внутри класса
                do {
                    if (++i == k) {
                        return stepName;
                    }
                    stackTraceElement = stackTraceElements[i];
                    nextClass = Class.forName(stackTraceElement.getClassName());
                    zephyrStep = scanClassMethodsForStep(nextClass, stackTraceElement.getMethodName());
                    if (zephyrStep != null) {
                        stepName = zephyrStep.description();
                    }
                } while (testClass.equals(nextClass) || nextClass.isAssignableFrom(testClass));
            }
        }
        return stepName;
    }

    private ZephyrStep scanClassMethodsForStep(Class<?> elementClass, String methodName) {
        final Method[] methods = elementClass.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            final Method method = methods[i];
            if (method.getName().equals(methodName)) {
                final ZephyrStep zephyrScaleStepAnnotation = method.getDeclaredAnnotation(ZephyrStep.class);
                if (zephyrScaleStepAnnotation != null) {
                    System.out.printf("Found: {step: %s, class: %s, method: %s}\n", zephyrScaleStepAnnotation.value(),
                            elementClass.getSimpleName(), method.getName());
                    return zephyrScaleStepAnnotation;
                }
            }
        }
        return null;
    }
}

