package com.tisawesomeness.minecord.setting;

import lombok.NonNull;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.annotation.Nullable;
import java.util.Optional;

public abstract class ChannelSetting<T> extends Setting<T> {

    /**
     * Gets the value of this setting for the text channel.
     * @param id The ID of the Discord text channel.
     * @return The value of the setting, or null if unset.
     */
    public abstract Optional<T> getChannel(long id);
    /**
     * <p>Gets the value of this setting used in the current context.</p>
     * This will get the setting for the current channel, or unset for DMs.
     * @param e The event that triggered the executing command.
     * @return The value of the setting, or null if unset.
     */
    public Optional<T> get(@NonNull MessageReceivedEvent e) {
        return e.isFromGuild() ? getChannel(e.getTextChannel().getIdLong()) : Optional.empty();
    }

    /**
     * Gets the effective value of this setting for the text channel.
     * @param id The ID of the Discord text channel.
     * @return The value of the setting, or the default if unset.
     */
    public @NonNull T getEffectiveChannel(long id) {
        return getChannel(id).orElse(getDefault());
    }

    /**
     * Changes the setting for the specified text channel.
     * @param id The ID of the discord text channel.
     * @param setting The value of the setting.
     * @return Whether the setting could be changed.
     */
    protected abstract boolean changeChannel(long id, @NonNull T setting);
    private @NonNull SetResult setChannelInternal(long id, Optional<T> from, ResolveResult<T> toResult) {
        Optional<T> toOpt = toResult.value;
        if (!toOpt.isPresent()) {
            return toResult.toStatus();
        }
        T to = toOpt.get();

        if (from.isPresent()) {
            if (to.equals(getDefault())) {
                return changeChannel(id, to) ? SetStatus.SET_FROM_TO_DEFAULT : SetStatus.INTERNAL_FAILURE;
            } else if (to.equals(from.get())) {
                return SetStatus.SET_NO_CHANGE;
            }
        } else if (to.equals(getDefault())) {
            return changeChannel(id, to) ? SetStatus.SET_TO_DEFAULT : SetStatus.INTERNAL_FAILURE;
        }
        return changeChannel(id, to) ? SetStatus.SET : SetStatus.INTERNAL_FAILURE;
    }
    /**
     * Changes this setting for the text channel.
     * @param id The ID of the Discord text channel.
     * @param input The user-provided input to change the setting to. Resets if {@code null}.
     * @return The string describing the result of the set operation.
     */
    public @NonNull String setChannel(long id, @Nullable String input) {
        if (input == null) {
            return resetChannel(id);
        }
        Optional<T> from = getChannel(id);
        ResolveResult<T> toResult = resolve(input);
        return setChannelInternal(id, from, toResult).getMsg(getDisplayName(),
                from.orElse(getDefault()).toString(), toResult.value.orElse(getDefault()).toString());
    }

    /**
     * <p>Resets the setting for the text channel, leaving it unset. {@link #getChannel(long id)} will return {@link #getDefault()}.</p>
     * <b>This is NOT equivalent to changing the setting to the default!</b> If the default value changes, then the setting for this channel will change also.
     * @param id The ID of the discord text channel.
     * @return Whether the setting could be changed.
     */
    protected abstract boolean clearChannel(long id);
    private @NonNull SetResult resetChannelInternal(long id, Optional<T> from) {
        if (!from.isPresent()) {
            return SetStatus.RESET_NO_CHANGE;
        } else if (from.get() == getDefault()) {
            return SetStatus.RESET_TO_DEFAULT;
        }
        return clearChannel(id) ? SetStatus.RESET : SetStatus.INTERNAL_FAILURE;
    }
    /**
     * Resets this setting for the text channel. This "unsets" the setting and does NOT change it to the default.
     * @param id The ID of the Discord text channel.
     * @return The string describing the result of the set operation.
     */
    public @NonNull String resetChannel(long id) {
        Optional<T> from = getChannel(id);
        return resetChannelInternal(id, from).getMsg(getDisplayName(),
                from.orElse(getDefault()).toString(), "");
    }

}
