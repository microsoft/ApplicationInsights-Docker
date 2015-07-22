package com.microsoft.applicationinsights.agent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by yonisha on 7/22/2015.
 */
public class PythonBootstraper {

    private String pythonScriptName;

    public PythonBootstraper(String pythonScriptName) {
        this.pythonScriptName = pythonScriptName;
    }

    public void start() {
        ProcessBuilder pb = new ProcessBuilder("python", this.pythonScriptName);

        try {
            Process p = pb.start();

            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String ret = in.readLine();
            System.out.println("JSON : " + ret);
        } catch (IOException e) {
            System.out.println(this.getClass().getSimpleName() + " failed with error: " + e.getMessage());
        }
    }
}
