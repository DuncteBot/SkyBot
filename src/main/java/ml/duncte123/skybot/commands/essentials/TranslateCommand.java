package ml.duncte123.skybot.commands.essentials;

import me.duncte123.botCommons.web.WebUtils;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.commands.image.ImageCommandBase;
import ml.duncte123.skybot.objects.command.CommandContext;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;

import java.util.List;

import static me.duncte123.botCommons.messaging.MessageUtils.sendMsg;

@Author(nickname = "sylmoss", author = "Sylvia Moss")
public class TranslateCommand extends ImageCommandBase {
    @Override
    public void executeCommand(@NotNull CommandContext ctx) {
        GuildMessageReceivedEvent event = ctx.getEvent();
        List<String> args = ctx.getArgs();

        if (args.isEmpty() || args.size() < 2) {
            sendMsg(event, "Correct usage: `" + PREFIX + getName() + "[destination language code] <text>`");
            return;
        }

        String targetLang = args.get(0);
        String input = String.join(" ", args.subList(1, args.size() - 1));
        JSONArray translatedJson = WebUtils.ins.translate("en", targetLang, input);

        if (translatedJson.length() < 1) {
            sendMsg(ctx.getEvent(), "No translation found");
            return;
        }

        String translation = translatedJson.getString(0);
        sendMsg(event, translation);
    }

    @Override
    public String getName() {
        return "translate";
    }

    @Override
    public String help() {
        return "Translate a text from English to another language\n"
                + "Usage: `" + PREFIX + getName() + "[destination language] <text>";
    }
}
