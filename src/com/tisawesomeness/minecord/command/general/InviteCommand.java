package com.tisawesomeness.minecord.command.general;

import java.awt.Color;

import com.tisawesomeness.minecord.Bot;
import com.tisawesomeness.minecord.Config;
import com.tisawesomeness.minecord.command.Command;
import com.tisawesomeness.minecord.util.MessageUtils;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class InviteCommand extends Command {
	
	public CommandInfo getInfo() {
		return new CommandInfo(
			"invite",
			"Invite the bot!",
			null,
			null,
			2000,
			false,
			false,
			true
		);
	}
	
	public Result run(String[] args, MessageReceivedEvent e) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.addField("Invite me!", Config.getInvite(), false);
		eb.addField("Help server", Bot.helpServer, false);
		eb.addField("Website", Bot.website, true);
		eb.setColor(Color.GREEN);
		eb = MessageUtils.addFooter(eb);
		return new Result(Outcome.SUCCESS, eb.build());
	}
	
}
