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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

@file:Author

package ml.duncte123.skybot

import ml.duncte123.skybot.commands.`fun`.*
import ml.duncte123.skybot.commands.animals.BirbCommand
import ml.duncte123.skybot.commands.essentials.RestartCommand
import ml.duncte123.skybot.commands.essentials.UpdateCommand
import ml.duncte123.skybot.commands.music.*
import ml.duncte123.skybot.commands.uncategorized.OneLinerCommands
import ml.duncte123.skybot.commands.uncategorized.ShortenCommand
import ml.duncte123.skybot.utils.AirUtils
import org.slf4j.event.Level

@SinceSkybot("3.50.4")
@Author
class KotlinCommandManager : CommandManager() {
    init {
        AirUtils.log("KotlinCommandManager", Level.INFO, "Registering kotlin commands")
        //uncategorized
        this.addCommand(OneLinerCommands())
        this.addCommand(ShortenCommand())
        //fun
        this.addCommand(BlobCommand())
        this.addCommand(CoinCommand())
        this.addCommand(TextToBricksCommand())
        this.addCommand(KpopCommand())
        this.addCommand(FlipCommand())
        this.addCommand(DialogCommand())
        this.addCommand(JokeCommand())
        this.addCommand(TagCommand())

        if(AirUtils.config.getString("apis.cleverbot.api") != null && !AirUtils.config.getString("apis.cleverbot.api").isEmpty()
                && AirUtils.config.getString("apis.cleverbot.user") != null && !AirUtils.config.getString("apis.cleverbot.user").isEmpty() )
            this.addCommand(ChatCommand())
        //animals
        this.addCommand(BirbCommand())
        //essentials
        this.addCommand(RestartCommand())
        this.addCommand(UpdateCommand())
        //music
        this.addCommand(JoinCommand())
        this.addCommand(LeaveCommand())
        this.addCommand(ListCommand())
        this.addCommand(NowPlayingCommand())
        this.addCommand(PauseCommand())
        this.addCommand(PlayCommand())
        this.addCommand(PlayRawCommand())
        this.addCommand(PPlayCommand())
        this.addCommand(RadioCommand())
        this.addCommand(RepeatCommand())
        this.addCommand(ShuffleCommand())
        this.addCommand(SkipCommand())
        this.addCommand(StopCommand())
    }
}