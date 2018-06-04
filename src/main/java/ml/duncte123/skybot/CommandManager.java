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

package ml.duncte123.skybot;

import kotlin.Triple;
import ml.duncte123.skybot.exceptions.VRCubeException;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.custom.CustomCommand;
import ml.duncte123.skybot.objects.command.custom.CustomCommandImpl;
import ml.duncte123.skybot.unstable.utils.ComparatingUtils;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.GuildSettingsUtils;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.reflections.Reflections;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SuppressWarnings("WeakerAccess")
public class CommandManager {

    /**
     * This stores all our commands
     */
    private final Set<Command> commands = ConcurrentHashMap.newKeySet();
    private final Set<CustomCommand> customCommands = ConcurrentHashMap.newKeySet();

    /**
     * This makes sure that all the commands are added
     */
    public CommandManager() {
        //Get reflections for this project
        registerCommandsFromReflection(new Reflections("ml.duncte123.skybot.commands"));
        registerCommandsFromReflection(new Reflections("ml.duncte123.skybot.unstable.commands"));

        loadCustomCommands();
    }

    /**
     * This is method to get the commands on request
     *
     * @return A list of all the commands
     */
    public Set<Command> getCommands() {
        return commands;
    }

    public Set<CustomCommand> getCustomCommands() {
        return customCommands;
    }

    /**
     * This tries to get a command with the provided name/alias
     *
     * @param name the name of the command
     * @return a possible null command for the name
     */
    public Command getCommand(String name) {
        Optional<Command> cmd = commands.stream().filter(c -> c.getName().equals(name)).findFirst();

        if (!cmd.isPresent()) {
            cmd = commands.stream().filter(c -> Arrays.asList(c.getAliases()).contains(name)).findFirst();
        }

        return cmd.orElse(null);
    }

    public List<Command> getCommands(CommandCategory category) {
        return commands.stream().filter(c -> c.getCategory().equals(category)).collect(Collectors.toList());
    }


    public CustomCommand getCustomCommand(String invoke, String guildId) {
        return customCommands.stream().filter(c -> c.getGuildId().equals(guildId))
                .filter(c -> c.getName().equalsIgnoreCase(invoke)).findFirst().orElse(null);
    }

    public boolean editCustomCommand(CustomCommand c) {
        return addCustomCommand(c, true, true).getFirst();
    }

    public Triple<Boolean, Boolean, Boolean> addCustomCommand(CustomCommand c) {
        return addCustomCommand(c, true, false);
    }

    public Triple<Boolean, Boolean, Boolean> addCustomCommand(CustomCommand command, boolean insertInDb, boolean isEdit) {
        if (command.getName().contains(" ")) {
            throw new VRCubeException("Name can't have spaces!");
        }

        boolean commandFound = this.customCommands.stream()
                .anyMatch((cmd) -> cmd.getName().equalsIgnoreCase(command.getName()) && cmd.getGuildId().equals(command.getGuildId())) && !isEdit;
        boolean limitReached = this.customCommands.stream().filter((cmd) -> cmd.getGuildId().equals(command.getGuildId())).count() >= 50 && !isEdit;

        if (commandFound || limitReached) {
            return new Triple<>(false, commandFound, limitReached);
        }

        if (insertInDb) {
            try {
                Triple<Boolean, Boolean, Boolean> res = AirUtils.DB.run(() -> {
                    Connection conn = AirUtils.DB.getConnManager().getConnection();

                    String sqlQuerry = (isEdit) ?
                            "UPDATE customCommands SET message = ? WHERE guildId = ? AND invoke = ?" :
                            "INSERT INTO customCommands(guildId, invoke, message) VALUES (? , ? , ?)";

                    try {
                        PreparedStatement stm = conn.prepareStatement(sqlQuerry);
                        stm.setString((isEdit) ? 2 : 1, command.getGuildId());
                        stm.setString((isEdit) ? 3 : 2, command.getName());
                        stm.setString((isEdit) ? 1 : 3, command.getMessage());
                        stm.execute();
                    } catch (SQLException e) {
                        e.printStackTrace();
                        return new Triple<>(false, false, false);
                    } finally {
                        try {
                            conn.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                    return null;
                }).get();

                if (res != null && !res.getFirst()) {
                    return res;
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        if (isEdit) {
            this.customCommands.remove(getCustomCommand(command.getName(), command.getGuildId()));
        }
        this.customCommands.add(command);

        return new Triple<>(true, false, false);
    }

    /**
     * This removes a command from the commands
     *
     * @param command the command to remove
     * @return {@code true} on success
     */
    public boolean removeCommand(String command) {
        return commands.remove(getCommand(command));
    }

    public boolean removeCustomCommand(String name, String guildId) {
        CustomCommand cmd = getCustomCommand(name, guildId);
        if (cmd == null)
            return false;

        try {
            return AirUtils.DB.run(() -> {
                Connection con = AirUtils.DB.getConnManager().getConnection();

                try {
                    PreparedStatement stm = con.prepareStatement("DELETE FROM customCommands WHERE invoke = ? AND guildId = ?");
                    stm.setString(1, name);
                    stm.setString(2, guildId);
                    stm.execute();
                } catch (SQLException e) {
                    e.printStackTrace();
                    return false;
                } finally {
                    try {
                        con.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }

                this.customCommands.remove(cmd);

                return true;
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * This handles adding the command
     *
     * @param command The command to add
     * @return true if the command is added
     */
    @SuppressWarnings("UnusedReturnValue")
    public boolean addCommand(Command command) {
        if (command.getName().contains(" ")) {
            throw new VRCubeException("Name can't have spaces!");
        }

        if (this.commands.stream().anyMatch((cmd) -> cmd.getName().equalsIgnoreCase(command.getName()))) {
            @SinceSkybot(version = "3.52.1")
            List<String> aliases = Arrays.asList(this.commands.stream().filter((cmd) -> cmd.getName().equalsIgnoreCase(command.getName())).findFirst().get().getAliases());
            for (String alias : command.getAliases()) {
                if (aliases.contains(alias)) {
                    return false;
                }
            }
            return false;
        }
        this.commands.add(command);

        return true;
    }

    /**
     * This will run the command when we need them
     *
     * @param event the event for the message
     */
    public void runCommand(GuildMessageReceivedEvent event) {
        final String[] split = event.getMessage().getContentRaw().replaceFirst(
                "(?i)" + Pattern.quote(Settings.PREFIX) + "|" + Pattern.quote(Settings.OTHER_PREFIX) + "|" +
                        Pattern.quote(GuildSettingsUtils.getGuild(event.getGuild()).getCustomPrefix()),
                "").split("\\s+");
        final String invoke = split[0].toLowerCase();

        dispatchCommand(invoke, Arrays.copyOfRange(split, 1, split.length), event);
    }

    public void dispatchCommand(String invoke, String[] args, GuildMessageReceivedEvent event) {
        Command cmd = getCommand(invoke);
        if (cmd == null) {
            cmd = (Command) getCustomCommand(invoke, event.getGuild().getId());
        }
        dispatchCommand(cmd, invoke, args, event);
    }

    public void dispatchCommand(Command cmd, String invoke, String[] args, GuildMessageReceivedEvent event) {
        if (cmd != null) {
            try {
                cmd.executeCommand(invoke, args, event);
            } catch (Throwable ex) {
                ComparatingUtils.execCheck(ex);
            }
        }
    }

    private void registerCommandsFromReflection(Reflections reflections) {
        //Loop over them commands
        for (Class<? extends Command> cmd : reflections.getSubTypesOf(Command.class)) {
            try {
                //Add the command
                this.addCommand(cmd.getDeclaredConstructor().newInstance());
            } catch (Exception ignored) {
            }
        }
    }

    private void loadCustomCommands() {
        AirUtils.DB.run(() -> {
            Connection con = AirUtils.DB.getConnManager().getConnection();
            try {
                ResultSet res = con.createStatement().executeQuery("SELECT * FROM customCommands");
                while (res.next()) {
                    addCustomCommand(new CustomCommandImpl(
                            res.getString("invoke"),
                            res.getString("message"),
                            res.getString("guildId")
                    ), false, false);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
