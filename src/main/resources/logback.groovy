/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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

import ch.qos.logback.classic.encoder.PatternLayoutEncoder

import static ch.qos.logback.classic.Level.DEBUG

def logToFile = false

appender("STDOUT", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        //this one looks nice: [%red(%X{jda.shard.id}) / %red(%X{jda.shard.total})]
        //more nice stuff: %d{dd-MM-yyyy HH:mm:ss} %boldCyan(%-32.-32thread) %red(%X{jda.shard.id}) / %red(%X{jda.shard.total}) %boldGreen(%-15.-15logger{0}) %highlight(%-6level) %msg%n
        //pattern = "[%d{dd-MM-yyyy HH:mm:ss, -5}] [%boldCyan(%thread)] [%boldGreen(%logger{36})] %red(%X{jda.shard}) %level - %msg%n"
        pattern = "%d{dd-MM-yyyy HH:mm:ss} %boldCyan(%-32.-32thread) %red(%X{jda.shard.id}) / %red(%X{jda.shard.total}) %boldGreen(%-15.-15logger{0}) %highlight(%-6level) %msg%n"
    }
}
root(INFO, ["STDOUT"])
//root(DEBUG, ["STDOUT"])


if(logToFile) {
    def bySecond = timestamp("yyyyMMdd'T'HHmmss")

    appender("FILE", FileAppender) {
        file = "log-${bySecond}-debug.txt"
        encoder(PatternLayoutEncoder) {
            pattern = "[%d{dd-MM-yyyy HH:mm:ss, -5}] [%boldCyan(%thread)] [%boldGreen(%logger{36})] %red(%X{jda.shard}) %level - %msg%n"
        }
    }
    root(DEBUG, ["FILE"])
}


