/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2019  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.almightyalpaca.discord.jdabutler.util.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.status.ErrorStatus;
import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * All credit goes to
 *
 * https://github.com/Almighty-Alpaca/JDA-Butler/blob/master/bot/src/main/java/com/almightyalpaca/discord/jdabutler/util/logging/WebhookAppender.java
 */
public class WebhookAppender extends AppenderBase<ILoggingEvent> {
    private static final Pattern WH_PATTERN = Pattern.compile("(?:https?://)?(?:\\w+\\.)?discordapp\\.com/api(?:/v\\d+)?/webhooks/(\\d+)/([\\w-]+)(?:/(?:\\w+)?)?");

    private Encoder<ILoggingEvent> encoder;
    private String webhookUrl;
    private WebhookClient client;

    @Override
    public void start() {
        int warn = 0;
        if(encoder == null) {
            addStatus(new ErrorStatus("No encoder specified", this));
            warn++;
        }
        Matcher matcher = null;
        if(webhookUrl == null || webhookUrl.isEmpty()) {
            addStatus(new ErrorStatus("No Webhook url specified", this));
            warn++;
        } else {
            matcher = WH_PATTERN.matcher(webhookUrl);
            if(!matcher.matches()) {
                addStatus(new ErrorStatus("Webhook url was not a valid Webhook url", this));
                warn++;
            }
        }
        if(warn == 0) {
            client = new WebhookClientBuilder(Long.parseUnsignedLong(matcher.group(1)), matcher.group(2)).setDaemon(true).build();
            super.start();
        }
    }

    @Override
    public void stop() {
        if(client != null) {
            client.close();
        }

        super.stop();
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        if(!isStarted()) {
            return;
        }

        if(eventObject.getLoggerName().equals("club.minnced.discord.webhook.WebhookClient")) {
            return;
        }

        final byte[] encode = encoder.encode(eventObject);
        final String log = new String(encode);
        client.send(log.length() > 2000 ? log.substring(0, 1997) + "..." : log);
    }

    public String getWebhookUrl() {
        return webhookUrl;
    }

    public void setWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public Encoder<ILoggingEvent> getEncoder() {
        return encoder;
    }

    public void setEncoder(Encoder<ILoggingEvent> encoder) {
        this.encoder = encoder;
    }
}
