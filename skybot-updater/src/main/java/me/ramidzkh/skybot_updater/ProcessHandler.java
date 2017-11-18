package me.ramidzkh.skybot_updater;

import org.apache.commons.io.input.TeeInputStream;
import org.apache.commons.io.output.TeeOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

public class ProcessHandler {

    public static final PrintStream
            out = System.out,
            err = System.err;
    public static final InputStream
            in = System.in;

    Process process;

    Thread bindA, bindB, bindC;

    public ProcessHandler()
    throws IOException {
        process = Runtime.getRuntime().exec(new String[] {Main.java, "-jar", "skybot.jar"});
    }

    public void bind() {
        bindA = new Thread(() -> {
            try {
                while (true)
                    process.getOutputStream().write(in.read());
            }  catch (IOException e) {
                e.printStackTrace(err);
            }
        });
    
        bindB = new Thread(() -> {
            try {
                while(true)
                    err.write(process.getErrorStream().read());
            } catch (IOException e) {
                e.printStackTrace(err);
            }
        });
    
        bindC = new Thread(() -> {
            try {
                while(true)
                    out.write(process.getInputStream().read());
            } catch (IOException e) {
                e.printStackTrace(err);
            }
        });
        
        bindA.start();
        bindB.start();
        bindC.start();
    }

    public void kill() {
        bindA.interrupt();
        bindB.interrupt();
        bindC.interrupt();
    
        System.setIn(in);
        System.setOut(out);
        System.setErr(err);
    }
}
