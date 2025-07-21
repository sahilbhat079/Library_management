package com.company.MultiModule;

import java.lang.reflect.Method;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        try {
            // Load AppStart using reflection
            Class<?> appClass = Class.forName("com.company.MultiModule.starter.AppStart");

            // Create instance
            Object appInstance = appClass.getDeclaredConstructor().newInstance();

            // Invoke run() method
            Method runMethod = appClass.getMethod("run");
            runMethod.invoke(appInstance);
        } catch (Exception e) {
            System.err.println(" Failed to launch AppStart using reflection: " + e.getMessage());
            e.printStackTrace();
        }
    }
}