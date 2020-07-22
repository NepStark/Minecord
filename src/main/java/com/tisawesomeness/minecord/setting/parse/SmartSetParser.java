package com.tisawesomeness.minecord.setting.parse;

import com.tisawesomeness.minecord.command.Command;
import com.tisawesomeness.minecord.command.CommandContext;
import com.tisawesomeness.minecord.database.dao.DbChannel;
import com.tisawesomeness.minecord.database.dao.DbUser;
import com.tisawesomeness.minecord.setting.Setting;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Changes a setting for the guild if executed in a server, or for the user if executed in DMs.
 * <br>Will fail if there is a channel override (since that should be changed instead).
 */
@RequiredArgsConstructor
public class SmartSetParser {
    @Getter private final @NonNull CommandContext txt;
    @Getter private final @NonNull Setting<?> setting;

    /**
     * Tries to change the provided setting.
     * @return The result of the command
     */
    public Command.Result parse() {
        if (!txt.e.isFromGuild()) {
            DbUser user = txt.getUser(txt.e.getAuthor());
            return new SettingChanger(this, user).parse();
        } else if (!SettingCommandHandler.userHasManageServerPermissions(txt.e)) {
            return new Command.Result(Command.Outcome.WARNING, ":warning: You must have Manage Server permissions.");
        }
        return changeIfNoChannelOverrides();
    }
    private Command.Result changeIfNoChannelOverrides() {
        DbChannel channel = txt.getChannel(txt.e.getTextChannel());
        if (setting.get(channel).isPresent()) {
            String name = setting.getDisplayName().toLowerCase();
            String msg = String.format("The %s setting has a channel override in this channel.\n" +
                    "Use `%sset channel #%s %s <value>` to change it.",
                    name, txt.prefix, txt.e.getChannel().getName(), name);
            return new Command.Result(Command.Outcome.SUCCESS, msg);
        }
        return new SettingChanger(this, txt.getGuild(txt.e.getGuild())).parse();
    }
}
