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

package ml.duncte123.skybot.commands.guild;

import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandContext;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class GuildJoinsCommand extends Command {
    @Override
    public void executeCommand(@Nonnull CommandContext ctx) {
        if (!ctx.getSelfMember().hasPermission(Permission.MESSAGE_ATTACH_FILES)) {
            sendMsg(ctx, "I need the `Attach Files` permission in order for this command to work");

            return;
        }

        final long startTime = ctx.getGuild().getCreationTime().toEpochSecond();
        final OffsetDateTime now = new Date().toInstant().atOffset(ZoneOffset.UTC);
        final long currentTime = now.toEpochSecond();
        final int imageWidth = 1000;
        final int imageHeight = 600;
        final List<Member> members = new ArrayList<>(ctx.getGuild().getMemberCache().asList());
        final BufferedImage bufferedImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
        final Graphics2D graphics2D = bufferedImage.createGraphics();

        members.sort(Comparator.comparing(Member::getJoinDate));
        graphics2D.setColor(Color.BLACK);
        graphics2D.fillRect(0, 0, imageWidth, imageHeight);

        double lastXPos = 0;
        int lastYPos = imageHeight;
        final int membersSize = members.size();

        for (int i = 0; i < membersSize; i++) {
            final long joinMinStart = members.get(i).getJoinDate().toEpochSecond() - startTime;
            final long joinTimesWidth = joinMinStart * imageWidth;
            final double xPos = joinTimesWidth / (currentTime - startTime);
            final int yPos = imageHeight - ((i * imageHeight) / membersSize);
            final double angle = xPos == lastXPos ? 1 : Math.tan((lastYPos - yPos) / (xPos - lastXPos)) / (Math.PI / 2);

            graphics2D.setColor(Color.getHSBColor((float) angle / 4, 1.0f, 1.0f));
            graphics2D.drawLine((int) xPos, yPos, (int) lastXPos, lastYPos);

            lastXPos = xPos;
            lastYPos = yPos;
        }

        graphics2D.setFont(graphics2D.getFont().deriveFont(24f));
        graphics2D.setColor(Color.WHITE);
        graphics2D.drawString("0 - " + membersSize + " Users", 20, 26);
        graphics2D.drawString(ctx.getGuild().getCreationTime().format(DateTimeFormatter.RFC_1123_DATE_TIME), 20, 60);
        graphics2D.drawString(now.format(DateTimeFormatter.RFC_1123_DATE_TIME), 20, 90);

        try {
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            ImageIO.write(bufferedImage, "png", outputStream);

            ctx.getChannel()
                .sendFile(outputStream.toByteArray(), "joins-for-" + ctx.getGuild().getId() + ".png")
                .queue();
        }
        catch (IOException e) {
            sendMsg(ctx, "Could not generate join graph: " + e.getMessage());
        }
    }

    @Override
    public String getName() {
        return "serverjoins";
    }

    @Override
    public String help() {
        return "Shows a graph with the joins for this server.\n" +
            "This is not a full history as it only looks at the members that are currently in the server.";
    }
}
