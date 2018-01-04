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

package ml.duncte123.skybot.commands.fun;

import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.utils.FileUtils;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class BanneCommand extends Command {

    private BufferedImage TOPPART;
    private BufferedImage ROLES;
    private BufferedImage BOTTOMPART;

    private final Color WHITETHEME = Color.WHITE;
    private final Color HOVERCOLOR = new Color(155,155,155);
    private final Color TEXTCOLOR = Color.BLACK;

    public BanneCommand() {
        this.category = CommandCategory.FUN;
        try {
            this.TOPPART = ImageIO.read(FileUtils.getFileFromResources("images/top.png"));
            this.ROLES = ImageIO.read(FileUtils.getFileFromResources("images/rolesWord.png"));
            this.BOTTOMPART = ImageIO.read(FileUtils.getFileFromResources("images/bottom.png"));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void executeCommand(@NotNull String invoke, @NotNull String[] args, @NotNull GuildMessageReceivedEvent event) {
        User u = event.getAuthor();
        if (event.getMessage().getMentionedUsers().size() > 0) {
            u = event.getMessage().getMentionedUsers().get(0);
        }
        String userTag = String.format("%#s", u);
        Font FONT = new Font("Arial", Font.BOLD, 20-userTag.length()/3);

        /* top part */
        BufferedImage topImage = new BufferedImage(TOPPART.getWidth(), TOPPART.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D tagG = topImage.createGraphics();
        tagG.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        tagG.drawImage(TOPPART, 0, 0, null);
        tagG.setColor(WHITETHEME);
        tagG.setFont(FONT);
        tagG.drawString(userTag, 20, 25);
        /* top part */

        /* roles */
        BufferedImage rolesBi = new BufferedImage(ROLES.getWidth(), ROLES.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D rolesG = rolesBi.createGraphics();
        rolesG.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        rolesG.drawImage(ROLES, 0, 0, null);
        if(event.getGuild().getMember(u).getRoles().size() > 0) {
            Role r = event.getGuild().getMember(u).getRoles().get(0);
            rolesG.setColor(HOVERCOLOR);
            rolesG.drawRect(10, 15, r.getName().length() * 10 - r.getName().length(), 20);
            rolesG.setColor(TEXTCOLOR);
            rolesG.drawString(r.getName(), 18, 28);
        }
        /* roles */

        /* bottom part */
        BufferedImage bottomImage = new BufferedImage(BOTTOMPART.getWidth(), BOTTOMPART.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D tagB = bottomImage.createGraphics();
        tagB.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        tagB.drawImage(BOTTOMPART, 0, 0, null);
        /* top part */

        try {
            File banne = new File("banne" +System.currentTimeMillis() + ".png");
            ImageIO.write(joinBufferedImage(topImage, rolesBi, bottomImage), "png", banne);
            event.getChannel().sendFile(banne, "banne.png", null).queue();
            banne.deleteOnExit();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String help() {
        return "Usage: `" + this.PREFIX+getName()+" [@user]`";
    }

    @Override
    public String getName() {
        return "banne";
    }

    private BufferedImage joinBufferedImage(BufferedImage topPart,BufferedImage roles, BufferedImage bottomPart) {

        //do some calculate first
        int offset = 5;
        int wid = topPart.getWidth();
        int height = topPart.getHeight()+bottomPart.getHeight()+roles.getHeight();
        //create a new buffer and draw two image into the new image
        BufferedImage newImage = new BufferedImage(wid,height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = newImage.createGraphics();
        Color oldColor = g2.getColor();
        //fill background
        g2.setPaint(Color.WHITE);
        g2.fillRect(0, 0, wid, height);
        //draw image
        g2.setColor(oldColor);
        g2.drawImage(topPart, null, 0, 0);
        g2.drawImage(roles, null, 0, topPart.getHeight());
        g2.drawImage(bottomPart, null, 0, topPart.getHeight()+roles.getHeight());
        g2.dispose();
        return newImage;
    }
}
