package com.tisawesomeness.minecord.command.admin;

import com.tisawesomeness.minecord.command.Command;
import com.tisawesomeness.minecord.command.Module;

public abstract class AbstractAdminCommand extends Command {
    public Module getModule() {
        return Module.ADMIN;
    }
}
