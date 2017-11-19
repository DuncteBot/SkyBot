package me.ramidzkh.skybot_updater;

import com.google.gson.JsonObject;
import okhttp3.OkHttpClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Main {
    
    public static final String java;
    public static final File file = new File("skybot.jar");

    static {
        String j = System.getProperty("java");
        if(j == null) j = "C:\\Program Files\\Java\\jre1.8.0_131\\bin\\java.exe";
        
        java = j;
    }

    public static void main(String[] args) throws IOException {
        if(!file.exists())
            try {
                handleDownloadFile();
                System.out.println("Successfully downloaded latest JAR file!");
            } catch (IOException e) {
                System.err.println("Couldn't download latest JAR file");
                e.printStackTrace();
                
                System.exit(1);
            }
        
        ProcessHandler handler;
        
        while(true) {
            try {
                handler = new ProcessHandler();
                
                handler.bind();

                while(handler.process.isAlive());
                
                int exit = handler.returnCode();
                
                if(exit == 0x5454) {
                    System.out.println("\nInitiating update procedure");
                    handleDownloadFile();
                    System.out.println("Successfully downloaded latest JAR file!\nRetsarting bot...\n\n");
                } else {
                    System.out.printf("Program exited with exit code %s. Goodbye!", exit);
                    break;
                }
            } catch (IOException e) {
                System.err.printf("Failed starting SkyBot (%s -jar %s)%n", java, file.getName());
                System.exit(1);
            }
        }
    }

    public static void handleDownloadFile()
    throws IOException {
        if(file.exists())
            file.delete();
        OkHttpClient client = new OkHttpClient();
        
        UpdateInfo info = UpdateInfo.load();
        if(info == null)
            GithubRequester.downloadLatest(client, new FileOutputStream(file));
        else {
            JsonObject release = GithubRequester.getLatestRelease(client);
            JsonObject asset = GithubRequester.getAsset(release);
            
            if(info.olderThan(new UpdateInfo(release, asset)))
                GithubRequester.downloadLatest(client, new FileOutputStream(file));
        }
    }
}
