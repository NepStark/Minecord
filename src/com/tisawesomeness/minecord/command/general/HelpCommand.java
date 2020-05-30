package com.tisawesomeness.minecord.command.general;

import com.tisawesomeness.minecord.Bot;
import com.tisawesomeness.minecord.command.Command;
import com.tisawesomeness.minecord.command.Module;
import com.tisawesomeness.minecord.command.Registry;
import com.tisawesomeness.minecord.database.Database;
import com.tisawesomeness.minecord.util.MessageUtils;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class HelpCommand extends Command {
	
	public CommandInfo getInfo() {
		return new CommandInfo(
			"help",
			"Displays this help menu.",
			null,
			new String[]{
				"commands",
				"cmds"},
			0,
			false,
			false,
			true
		);
	}
	
	public Result run(String[] args, MessageReceivedEvent e) {
		String prefix = MessageUtils.getPrefix(e);
		
		//If the author used the admin keyword and is an elevated user
		boolean elevated = false;
		if (args.length > 0 && args[0].equals("admin") && Database.isElevated(e.getAuthor().getIdLong())) {
			elevated = true;
		}

		//Iterate through every registered command
		String m = "";
		for (Module mod : Registry.modules) {
			for (Command c : mod.getCommands()) {
				CommandInfo ci = c.getInfo();
				if (!ci.hidden || elevated) {
					//Fetch basic info
					String name = ci.name;
					String description = ci.description;
					
					//Add text objects
					if ("".equals(name)) {
						if (description != null && (!ci.elevated || elevated)) {
							m += description + "\n";
						}
						continue;
					}
					
					//Fetch info and build message line
					String usage = ci.usage;
					String gap = "";
					if (!"".equals(usage)) {
						gap = " ";
					}
					m += "`" + prefix + name + gap + usage + "` **-** " + description + "\n";
				}
			}
		}
		m = m.substring(0, m.length() - 1); //Remove trailing newline
		m += "\n\n" + "**Arguments:**" +
			"\n" + "`<>` - required, `[]` - optional, `?` - true/false.";
		
		MessageEmbed me = MessageUtils.embedMessage(null, null, m, Bot.color);
		
		return new Result(Outcome.SUCCESS, me);
	}

}
