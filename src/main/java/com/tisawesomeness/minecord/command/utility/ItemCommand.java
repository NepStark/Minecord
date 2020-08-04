package com.tisawesomeness.minecord.command.utility;

import com.tisawesomeness.minecord.command.CommandContext;
import com.tisawesomeness.minecord.item.Item;

import lombok.NonNull;
import net.dv8tion.jda.api.EmbedBuilder;

public class ItemCommand extends AbstractUtilityCommand {

	public @NonNull String getId() {
		return "item";
	}

	public Result run(CommandContext ctx) {
		// Check for argument length
		if (ctx.args.length == 0) {
			return ctx.showHelp();
		}
		
		// Search through the item database
		String item = Item.search(ctx.joinArgs(), "en_US");
		
		// If nothing is found
		if (item == null) {
			return new Result(Outcome.WARNING,
				":warning: That item does not exist! " +
				"\n" + "Did you spell it correctly?");
		}
		
		// Build message
		EmbedBuilder eb = Item.display(item, "en_US", ctx.prefix);
		eb.setFooter("See an error? Please report them at https://goo.gl/KWCxis", null);
		// eb = MessageUtils.addFooter(eb);
		
		return new Result(Outcome.SUCCESS, eb.build());
	}

}
