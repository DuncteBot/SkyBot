/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import ch.qos.logback.classic.boolex.GEventEvaluator
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.filter.ThresholdFilter
import ch.qos.logback.core.filter.EvaluatorFilter
import io.sentry.logback.SentryAppender

import static ch.qos.logback.core.spi.FilterReply.DENY
import static ch.qos.logback.core.spi.FilterReply.NEUTRAL

appender("STDOUT", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        //this one looks nice: [%red(%X{jda.shard.id}) / %red(%X{jda.shard.total})]
        //more nice stuff: %d{dd-MM-yyyy HH:mm:ss} %boldCyan(%-32.-32thread) %red(%X{jda.shard.id}) / %red(%X{jda.shard.total}) %boldGreen(%-15.-15logger{0}) %highlight(%-6level) %msg%n
        //pattern = "[%d{dd-MM-yyyy HH:mm:ss, -5}] [%boldCyan(%thread)] [%boldGreen(%logger{36})] %red(%X{jda.shard}) %level - %msg%n"
        pattern = "%d{dd-MM-yyyy HH:mm:ss} %boldCyan(%thread) %red(%X{jda.shard}) %boldGreen(%-15.-15logger{0}) %highlight(%-6level) %msg%n"
    }
}

appender("Sentry", SentryAppender) {
    filter(ThresholdFilter) {
        level = ERROR
    }

    filter(EvaluatorFilter) {
        evaluator(GEventEvaluator) {
            // Stuff to ignore
            expression = """e.loggerName == 'ml.duncte123.skybot.ShardWatcher' ||
                            e.loggerName == 'net.notfab.caching.client.CacheClient' ||
                            e.formattedMessage.startsWith('Got disconnected from WebSocket') || 
                            e.message.contains('Ignoring deprecated socket close linger time') ||
                            e.message.contains('Using SQLite as the database') ||
                            e.message.contains('Please note that is is not recommended for production')
                            """.stripMargin()
        }

        onMatch = DENY
        onMismatch = NEUTRAL
    }
}

root(INFO, ["STDOUT", "Sentry"])

//appender("FILE", FileAppender) {
//    file = "./lavaplayer.log"
//    append = true
//    encoder(PatternLayoutEncoder) {
//        pattern = "%level %logger - %msg%n"
//    }
//}
//
//root(DEBUG, ["STDOUT", "FILE"])

//logger('net.dv8tion.jda.internal.requests.WebSocketClient', TRACE)
//logger('net.notfab.caching.client.CacheClient', TRACE)
//logger('net.dv8tion.jda.internal.requests.RateLimiter', TRACE)


