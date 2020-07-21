package com.tisawesomeness.minecord.setting.parse;

import com.tisawesomeness.minecord.command.Command;
import com.tisawesomeness.minecord.command.CommandContext;
import com.tisawesomeness.minecord.setting.Setting;

import lombok.NonNull;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.util.function.Function;

/**
 * Handles the {@code &settings}, {@code &set}, and {@code &reset} commands.
 * <br>Subclasses contain all information needed for args processing and pass this information to each other in a chain.
 */
public abstract class SettingCommandHandler {
    /**
     * Gets the context of the executing command.
     * @return The context
     */
    public abstract @NonNull CommandContext getTxt();

    /**
     * Parses this handler's arguments.
     * <br>When done, may also call {@code parse()} in other handlers for more processing.
     * @return A command result that is shown to the user, which may be an error message
     */
    public abstract Command.Result parse();

    /**
     * Loops over all settings and displays their names, descriptions, and values in an embed.
     * @param title The title of the embed
     * @param displayFunction A function that takes in a setting and outputs a string.
     *                        This should call one of the {@code getDisplay()} functions with predefined arguments.
     * @return A result with a fully formatted, branded embed
     */
    protected Command.Result displaySettings(String title, Function<? super Setting<?>, String> displayFunction) {
        CommandContext txt = getTxt();
        EmbedBuilder eb = new EmbedBuilder().setTitle(title);
        String tag = txt.e.getJDA().getSelfUser().getAsTag();

        for (Setting<?> setting : txt.bot.getSettings()) {
            String description = setting.getDescription(txt.prefix, tag);
            String display = displayFunction.apply(setting);
            String formattedDisplay = MarkdownUtil.bold(MarkdownUtil.monospace(display));
            String field = description + "\nCurrent: " + formattedDisplay;
            eb.addField(setting.getDisplayName(), field, false);
        }
        return new Command.Result(Command.Outcome.SUCCESS, txt.brand(eb).build());
    }
}
