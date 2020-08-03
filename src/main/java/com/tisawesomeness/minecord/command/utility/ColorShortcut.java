package com.tisawesomeness.minecord.command.utility;

import com.tisawesomeness.minecord.Lang;
import com.tisawesomeness.minecord.command.Command;
import com.tisawesomeness.minecord.command.CommandContext;
import com.tisawesomeness.minecord.command.IShortcutCommand;
import com.tisawesomeness.minecord.util.ColorUtils;

import lombok.NonNull;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.Color;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ColorShortcut extends AbstractUtilityCommand implements IShortcutCommand {

    private final Command colorCmd;
    private final String colorCode;
    private final Color color;
    public ColorShortcut(Command colorCmd, int index) {
        this.colorCmd = colorCmd;
        colorCode = String.format("%01x", index);
        color = ColorUtils.getColor(index);
    }

    public @NonNull String getId() {
        return colorCode;
    }
    @Override
    public @NonNull String getDisplayName(Lang lang) {
        return colorCode;
    }
    @Override
    public @NonNull String getDescription(Lang lang) {
        return colorCmd.getDescription(lang);
    }
    @Override
    public Optional<String> getUsage(Lang lang) {
        return Optional.empty();
    }
    @Override
    public List<String> getAliases(Lang lang) {
        return Collections.emptyList();
    }

    public CommandInfo getInfo() {
		return new CommandInfo(
                true,
                false,
                true
		);
    }

    public @NonNull String getHelp(Lang lang, String prefix, String tag) {
        return colorCmd.getHelp(lang, prefix, tag);
    }

    public Result run(CommandContext ctx) {
        EmbedBuilder eb = ColorCommand.buildColorInfo(color);
        return new Result(Outcome.SUCCESS, ctx.addFooter(eb).build());
    }

}