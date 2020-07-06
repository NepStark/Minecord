package com.tisawesomeness.minecord.database;

import java.util.Optional;

/**
 * An object containing values for each {@link com.tisawesomeness.minecord.setting.Setting}.
 */
public interface SettingContainer extends DMSettingContainer {
    // Methods in subclass generated by lombok
    Optional<Boolean> getUseMenu();
    DMSettingContainer withUseMenu(Optional<Boolean> useMenus);
}