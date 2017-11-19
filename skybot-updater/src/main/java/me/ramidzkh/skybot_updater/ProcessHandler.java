package me.ramidzkh.skybot_updater;

import java.io.File;
import java.io.IOException;

public class ProcessHandler {

    Process process;

    public ProcessHandler()
    throws IOException {
        process = new ProcessBuilder()
                .command(Main.java, "-jar", "skybot.jar")
                .directory(new File(System.getProperty("user.dir")))
                .inheritIO()
                .start();
    }

    public void bind() {
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            System.exit(1);
        }
    }

    public int returnCode() {
        return process.exitValue();
    }

}
