package me.duncte123.skybot.utils;

import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpClientTools;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterfaceManager;
import com.sedmelluq.discord.lavaplayer.tools.io.ThreadLocalHttpInterfaceManager;
import org.apache.http.impl.client.HttpClientBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class DabYoutubeAudioSourceManager extends YoutubeAudioSourceManager {
    DabYoutubeAudioSourceManager() {
        super(true);

        try {
            // getting the httpInterfaceManager variable
            Field field = this.getClass().getSuperclass().getDeclaredField("httpInterfaceManager");
            field.setAccessible(true);

            // removing the final modifier
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

            // creating a new HttpClientBuilder with cookie management disabled
            HttpClientBuilder httpClientBuilder = HttpClientTools.createSharedCookiesHttpBuilder()
                    .disableCookieManagement();

            // creating a new HttpInterfaceManager with our HttpClientBuilder
            HttpInterfaceManager httpInterfaceManager = new ThreadLocalHttpInterfaceManager(httpClientBuilder,
                    HttpClientTools.DEFAULT_REQUEST_CONFIG);

            // setting the new value of the field
            field.set(this, httpInterfaceManager);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
