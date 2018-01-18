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
import ml.duncte123.skybot.utils.EmbedUtils

def quick_mafs(int x) {
   def the_thing = x + 2 -1 
   return the_thing
}

//channel.sendFile(new URL("https://pbs.twimg.com/profile_images/892463026003222529/so6nfXWX.jpg").openStream(), "filename.png", new MessageBuilder()
//.setEmbed(EmbedUtils.defaultEmbed().setImage("attachment://filename.png").build()).build()).queue()

channel.sendMessage("This has an embed with an image!")
             .addFile(new URL("https://pbs.twimg.com/profile_images/892463026003222529/so6nfXWX.jpg").openStream(), "alpaca.png")
             .embed(EmbedUtils.embedImage("attachment://alpaca.png"))
             .queue()

return quick_mafs(2) + "\nThe thing goes skrra"
