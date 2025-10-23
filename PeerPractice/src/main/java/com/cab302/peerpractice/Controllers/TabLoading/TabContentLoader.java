package com.cab302.peerpractice.Controllers.TabLoading;

import com.cab302.peerpractice.Model.Entities.Group;
import javafx.scene.control.Tab;

/**
 * Strategy interface for loading content into group tabs.
 * Implements the Strategy pattern to eliminate switch statements and enable
 * Open/Closed Principle - new tab types can be added without modifying existing code.
 */
@FunctionalInterface
public interface TabContentLoader {

    /**
     * Loads content into the specified tab for the given group.
     *
     * @param tab the tab to load content into
     * @param group the group context for the content
     */
    void loadContent(Tab tab, Group group);
}
