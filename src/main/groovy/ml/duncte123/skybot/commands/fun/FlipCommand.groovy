/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Sanduhr32
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

package ml.duncte123.skybot.commands.fun

import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent

class FlipCommand extends Command {

    FlipCommand() {
        this.category = CommandCategory.FUN
    }

    @Override
    void executeCommand(String invoke, String[] args, GuildMessageReceivedEvent event) {
        def uname = event.member.effectiveName
        def output = ""

        if (event.message.mentionedUsers.size() > 0) {
            uname = event.guild.getMember(event.message.mentionedUsers.get(0)).effectiveName
        } else if(args.size() > 0) {
            uname = args.join(" ")
        }

        String normal = "abcdefghijklmnopqrstuvwxyz_,;.?!/\\'"
        String split = "ɐqɔpǝɟbɥıظʞןɯuodbɹsʇnʌʍxʎz‾'؛˙¿¡/\\,"
        //maj
        normal += "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        split += "∀qϽᗡƎℲƃHIſʞ˥WNOԀὉᴚS⊥∩ΛMXʎZ"
        //number
        normal += "0123456789"
        split += "0ƖᄅƐㄣϛ9ㄥ86"


        uname = uname.reverse()

        char letter
        for (int i = 0; i < uname.length(); i++) {
            letter = uname.charAt(i)

            int a = normal.indexOf(letter.toString(), 0)
            output += (a != -1) ? split.charAt(a) : letter
        }

        sendMsg(event, "(╯°□°）╯︵ $output")
    }

    @Override
    String help() {
        return "Flips a user.\n" +
                "Usage: `$PREFIX$name [@user]`"
    }

    @Override
    String getName() {
        return "flip"
    }
}
