package com.tisawesomeness.minecord.command.config;

import com.tisawesomeness.minecord.command.Command;
import com.tisawesomeness.minecord.command.CommandContext;
import com.tisawesomeness.minecord.setting.parse.SettingCommandParser;
import com.tisawesomeness.minecord.setting.parse.SettingCommandType;

import lombok.NonNull;

public class ResetCommand extends AbstractConfigCommand {

    public @NonNull String getId() {
        return "reset";
    }
    public CommandInfo getInfo() {
        return new CommandInfo(
                false,
                false,
                false
        );
    }

    public Command.Result run(CommandContext ctx) {
        return new SettingCommandParser(ctx, SettingCommandType.RESET).parse();
    }
}