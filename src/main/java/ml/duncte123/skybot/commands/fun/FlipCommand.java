/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken
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

package ml.duncte123.skybot.commands.fun;

import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class FlipCommand extends Command {

    public FlipCommand() {
        this.category = CommandCategory.FUN;
    }

    @Override
    public void executeCommand(String invoke, String[] args, GuildMessageReceivedEvent event) {
        User u = event.getAuthor();
        String flippedUser = "";

        if( event.getMessage().getMentionedUsers().size() > 0 ) {
            u = event.getMessage().getMentionedUsers().get(0);
        }

        String normal = "abcdefghijklmnopqrstuvwxyz_,;.?!/\\'";
        String split  = "ɐqɔpǝɟbɥıظʞןɯuodbɹsʇnʌʍxʎz‾'؛˙¿¡/\\,";
        //maj
        normal += "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        split  += "∀qϽᗡƎℲƃHIſʞ˥WNOԀὉᴚS⊥∩ΛMXʎZ";
        //number
        normal += "0123456789";
        split  += "0ƖᄅƐㄣϛ9ㄥ86";

        String username = new StringBuilder().append(u.getName()).reverse().toString();

        char letter;
        for (int i=0; i< username.length(); i++) {
            letter = username.charAt(i);

            int a = normal.indexOf(letter);
            flippedUser += (a != -1) ? split.charAt(a) : letter;
        }

        sendMsg(event, "(╯°□°）╯︵ " + flippedUser);
    }

    @Override
    public String help() {
        return "Flips a user.\n" +
                "Usage: `"+this.PREFIX+getName()+" [@user]`";
    }

    @Override
    public String getName() {
        return "flip";
    }
}
