package com.tisawesomeness.minecord.setting;

import com.tisawesomeness.minecord.Config;
import com.tisawesomeness.minecord.setting.impl.PrefixSetting;
import com.tisawesomeness.minecord.setting.impl.UseMenusSetting;

import lombok.NonNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class SettingRegistry {
    public final @NonNull PrefixSetting prefix;
    public final @NonNull UseMenusSetting useMenus;
    public final List<Setting<?>> settingsList;
    public final List<DMSetting<?>> dmSettingsList;

    /**
     * Initializes all the settings and makes them searchable from this registry.
     * @param config The loaded config file with the setting defaults.
     */
    public SettingRegistry(@NonNull Config config) {
        prefix = new PrefixSetting(config);
        useMenus = new UseMenusSetting(config);
        settingsList = Arrays.asList(prefix, useMenus);
        dmSettingsList = Collections.singletonList(prefix);
    }

    /**
     * <p>Gets the setting that should be returned for {@code &setting input ...}</p>
     * Uses {@link ISetting#isAlias(String name)} to check if the input matches.
     * @param name The name or alias of the setting.
     * @return The setting if found, else empty.
     */
    public Optional<Setting<?>> getSetting(@NonNull String name) {
        for (Setting<?> setting : settingsList) {
            if (setting.isAlias(name)) {
                return Optional.of(setting);
            }
        }
        return Optional.empty();
    }

    /**
     * <p>Gets the setting that should be returned for {@code &setting input ...}</p>
     * Uses {@link ISetting#isAlias(String name)} to check if the input matches.
     * @param name The name or alias of the setting.
     * @return The setting if found, else empty.
     */
    public Optional<DMSetting<?>> getDMSetting(@NonNull String name) {
        for (DMSetting<?> setting : dmSettingsList) {
            if (setting.isAlias(name)) {
                return Optional.of(setting);
            }
        }
        return Optional.empty();
    }
}
