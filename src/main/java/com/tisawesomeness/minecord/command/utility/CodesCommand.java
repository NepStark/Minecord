package com.tisawesomeness.minecord.command.utility;

import com.tisawesomeness.minecord.command.CommandContext;
import com.tisawesomeness.minecord.util.ColorUtils;

import lombok.NonNull;
import net.dv8tion.jda.api.EmbedBuilder;

public class CodesCommand extends AbstractUtilityCommand {

	public @NonNull String getId() {
		return "codes";
	}
	public CommandInfo getInfo() {
		return new CommandInfo(
                false,
				false,
				true
		);
	}
	
	private String img = "https://minecraft.gamepedia.com/media/minecraft.gamepedia.com/7/7e/Minecraft_Formatting.gif";
	
	public Result run(CommandContext ctx) {
		
		String desc = String.format("Symbol copy-paste: `\u00A7`, `\\u00A7`\nUse `%scolor` to get info on a color.", ctx.prefix);
		EmbedBuilder eb = new EmbedBuilder()
			.setTitle("Minecraft Chat Codes")
			.setColor(ColorUtils.randomColor())
			.setDescription(desc)
			.setImage(img);
		return new Result(Outcome.SUCCESS, ctx.addFooter(eb).build());
		
	}

}
