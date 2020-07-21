package com.tisawesomeness.minecord.command;

import com.tisawesomeness.minecord.command.Command.CommandInfo;
import com.tisawesomeness.minecord.command.admin.BanCommand;
import com.tisawesomeness.minecord.command.admin.DebugCommand;
import com.tisawesomeness.minecord.command.admin.DemoteCommand;
import com.tisawesomeness.minecord.command.admin.EvalCommand;
import com.tisawesomeness.minecord.command.admin.MsgCommand;
import com.tisawesomeness.minecord.command.admin.NameCommand;
import com.tisawesomeness.minecord.command.admin.PromoteCommand;
import com.tisawesomeness.minecord.command.admin.ReloadCommand;
import com.tisawesomeness.minecord.command.admin.SayCommand;
import com.tisawesomeness.minecord.command.admin.ShutdownCommand;
import com.tisawesomeness.minecord.command.admin.TestCommand;
import com.tisawesomeness.minecord.command.admin.UsageCommand;
import com.tisawesomeness.minecord.command.general.GuildCommand;
import com.tisawesomeness.minecord.command.general.PermsCommand;
import com.tisawesomeness.minecord.command.general.PrefixCommand;
import com.tisawesomeness.minecord.command.general.PurgeCommand;
import com.tisawesomeness.minecord.command.general.ResetCommand;
import com.tisawesomeness.minecord.command.general.RoleCommand;
import com.tisawesomeness.minecord.command.general.RolesCommand;
import com.tisawesomeness.minecord.command.general.SetCommand;
import com.tisawesomeness.minecord.command.general.SettingsCommand;
import com.tisawesomeness.minecord.command.general.UserCommand;
import com.tisawesomeness.minecord.command.misc.CreditsCommand;
import com.tisawesomeness.minecord.command.misc.HelpCommand;
import com.tisawesomeness.minecord.command.misc.InfoCommand;
import com.tisawesomeness.minecord.command.misc.InviteCommand;
import com.tisawesomeness.minecord.command.misc.PingCommand;
import com.tisawesomeness.minecord.command.misc.VoteCommand;
import com.tisawesomeness.minecord.command.player.AvatarCommand;
import com.tisawesomeness.minecord.command.player.BodyCommand;
import com.tisawesomeness.minecord.command.player.CapeCommand;
import com.tisawesomeness.minecord.command.player.HeadCommand;
import com.tisawesomeness.minecord.command.player.HistoryCommand;
import com.tisawesomeness.minecord.command.player.ProfileCommand;
import com.tisawesomeness.minecord.command.player.SkinCommand;
import com.tisawesomeness.minecord.command.player.UuidCommand;
import com.tisawesomeness.minecord.command.utility.CodesCommand;
import com.tisawesomeness.minecord.command.utility.ColorCommand;
import com.tisawesomeness.minecord.command.utility.ColorShortcut;
import com.tisawesomeness.minecord.command.utility.IngredientCommand;
import com.tisawesomeness.minecord.command.utility.ItemCommand;
import com.tisawesomeness.minecord.command.utility.RecipeCommand;
import com.tisawesomeness.minecord.command.utility.SalesCommand;
import com.tisawesomeness.minecord.command.utility.ServerCommand;
import com.tisawesomeness.minecord.command.utility.Sha1Command;
import com.tisawesomeness.minecord.command.utility.StatusCommand;
import com.tisawesomeness.minecord.database.DatabaseCache;

import net.dv8tion.jda.api.sharding.ShardManager;

import java.util.LinkedHashMap;

/**
 * The list of all commands the bot knows.
 */
public class CommandRegistry {
	
	private static final String adminHelp = "**These commands require elevation to use.**\n\n" +
	"`{&}info admin` - Displays bot info, including used memory and boot time.\n" +
	"`{&}settings <guild id> admin [setting] [value]` - Change the bot's settings for another guild.\n" +
	"`{&}perms <channel id> admin` - Test the bot's permissions in any channel.\n" +
	"`{&}user <user id> admin [mutual]` - Show info, ban status, and elevation for a user outside of the current guild. Include \"mutual\" to show mutual guilds.\n" +
	"`{&}guild <guild id> admin` - Show info and ban status for another guild.\n";

	private final LinkedHashMap<String, Command> commandMap;
	public final Module[] modules;
	
	/**
	 * Adds every module to the registry and maps the possible aliases to the command to execute.
	 */
	public CommandRegistry(ShardManager sm, DatabaseCache dbCache) {

		Command colorCmd = new ColorCommand();
		modules = new Module[]{
				new Module("General",
						new GuildCommand(),
						new RoleCommand(),
						new RolesCommand(),
						new UserCommand(),
						new PurgeCommand(),
						new PermsCommand(),
						new PrefixCommand(),
						new SettingsCommand(),
						new SetCommand(),
						new ResetCommand()
				),
				new Module("Utility",
						new StatusCommand(),
						new SalesCommand(),
						new CodesCommand(),
						colorCmd,
						new ColorShortcut(colorCmd, "0"),
						new ColorShortcut(colorCmd, "1"),
						new ColorShortcut(colorCmd, "2"),
						new ColorShortcut(colorCmd, "3"),
						new ColorShortcut(colorCmd, "4"),
						new ColorShortcut(colorCmd, "5"),
						new ColorShortcut(colorCmd, "6"),
						new ColorShortcut(colorCmd, "7"),
						new ColorShortcut(colorCmd, "8"),
						new ColorShortcut(colorCmd, "9"),
						new ColorShortcut(colorCmd, "a"),
						new ColorShortcut(colorCmd, "b"),
						new ColorShortcut(colorCmd, "c"),
						new ColorShortcut(colorCmd, "d"),
						new ColorShortcut(colorCmd, "e"),
						new ColorShortcut(colorCmd, "f"),
						new ServerCommand(),
						new Sha1Command(),
						new ItemCommand(),
						new RecipeCommand(),
						new IngredientCommand()
				),
				new Module("Player",
						new UuidCommand(),
						new HistoryCommand(),
						new AvatarCommand(),
						new HeadCommand(),
						new BodyCommand(),
						new SkinCommand(),
						new CapeCommand(),
						new ProfileCommand()
				),
				new Module("Misc",
						new HelpCommand(this),
						new InfoCommand(),
						new PingCommand(),
						new InviteCommand(),
						new VoteCommand(),
						new CreditsCommand()
				),
				new Module("Admin", true, adminHelp,
						new SayCommand(),
						new MsgCommand(),
						new NameCommand(),
						new UsageCommand(this),
						new PromoteCommand(),
						new DemoteCommand(),
						new BanCommand(),
						new ReloadCommand(),
						new ShutdownCommand(),
						new EvalCommand(),
						new DebugCommand(sm, dbCache),
						new TestCommand()
				)
		};

		commandMap = new LinkedHashMap<>();
		mapCommands();

	}
	private void mapCommands() {
		for (Module m : modules) {
			for (Command c : m.getCommands()) {
				CommandInfo ci = c.getInfo();
				commandMap.put(ci.name, c);
				for (String alias : ci.aliases) {
					commandMap.put(alias, c);
				}
			}
		}
	}

	/**
	 * Gets a module, given its name
	 * @param name Case-insensitive name of the desired module
	 * @return The module, or null if not found.
	 */
	public Module getModule(String name) {
		for (Module m : modules) {
			if (m.getName().equalsIgnoreCase(name)) {
				return m;
			}
		}
		return null;
	}
	/**
	 * Gets a command, given its name or alias.
	 * @param name The part of the command after "&" and before a space. For example, "&server hypixel.net" becomes "server".
	 * @return The command which should be executed, or null if there is no command associated with the input.
	 */
	public Command getCommand(String name) {
		return commandMap.get(name);
	}
	/**
	 * Gets the module a command belongs to
	 * @param cmdName Case-sensitive name of the command
	 * @return The module, or null if not found. This should never return null unless the command name is incorrect or a command was registered without a module.
	 */
	public String findModuleName(String cmdName) {
		for (Module m : modules) {
			for (Command c : m.getCommands()) {
				if (c.getInfo().name.equals(cmdName)) {
					return m.getName();
				}
			}
		}
		return null;
	}

}
