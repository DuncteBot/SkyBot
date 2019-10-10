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

package ml.duncte123.skybot.commands.fun;

import me.duncte123.botcommons.messaging.EmbedUtils;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;

import javax.annotation.Nonnull;

import static me.duncte123.botcommons.messaging.MessageUtils.sendEmbed;

public class CrashCommand extends Command {

    private final String[] errors = {
        "https://i.stack.imgur.com/U5hlt.jpg", "https://i.stack.imgur.com/D1xma.png",
        "http://3.bp.blogspot.com/-ISOa-ufScRA/UAcPBaTYXaI/AAAAAAAAAFc/ox0dZ18we6M/s1600/Screen+Shot+2012-07-17+at+1.15.17+AM.png",
        "http://www.itconsultants.com.au/media/1515/blue-screen-of-death.jpg", "http://www.winhelponline.com/blog/wp-content/uploads/2016/12/gsod-crash-screen-1.png",
        "http://www.terinea.co.uk/blog/wp-content/uploads/2007/06/image47.png", "https://i.stack.imgur.com/Tliko.jpg",
        "http://www.appleallen.net/crashnburn/files/page5_2.jpg", "https://www.techworm.net/wp-content/uploads/2016/07/Untitled-6.png",
        "https://cdn3.techadvisor.co.uk/cmsdata/features/3624863/BSoD_in_Windows_8_thumb800.png", "http://www.alltecheasy.com/wp-content/uploads/2015/09/Blue-Screen-of-Death-Error-1.jpg",
        "https://filestore.community.support.microsoft.com/api/images/f2823f3c-226c-4270-b2c3-ea152e9985d3"
    };

    public CrashCommand() {
        this.category = CommandCategory.FUN;
        this.name = "crash";
        this.helpFunction = (prefix, invoke) -> "Crashes the bot";
    }

    @Override
    public void execute(@Nonnull CommandContext ctx) {
        sendEmbed(ctx, EmbedUtils.embedImage(errors[ctx.getRandom().nextInt(errors.length)]).setTitle("DuncteBot crashed:"));
    }
}
