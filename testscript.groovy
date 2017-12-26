import net.dv8tion.jda.core.*;
import ml.duncte123.skybot.utils.*;

def quick_mafs(int x) {
   def the_thing = x + 2 -1 
   return the_thing
}

//channel.sendFile(new URL("https://pbs.twimg.com/profile_images/892463026003222529/so6nfXWX.jpg").openStream(), "filename.png", new MessageBuilder()
//.setEmbed(EmbedUtils.defaultEmbed().setImage("attachment://filename.png").build()).build()).queue()

channel.sendMessage("This has an embed with an image!")
             .addFile(new URL("https://pbs.twimg.com/profile_images/892463026003222529/so6nfXWX.jpg").openStream(), "alpaca.png")
             .embed(EmbedUtils.embedImage("attachment://alpaca.png"))
             .queue();

return quick_mafs(2) + "\nThe thing goes skrra"
