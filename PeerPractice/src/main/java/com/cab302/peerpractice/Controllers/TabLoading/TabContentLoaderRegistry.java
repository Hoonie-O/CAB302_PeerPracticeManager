package com.cab302.peerpractice.Controllers.TabLoading;

import com.cab302.peerpractice.Model.Entities.Group;
import javafx.scene.control.Tab;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Registry for tab content loaders using the Strategy pattern.
 * Eliminates switch statements and provides extensibility for new tab types.
 *
 * This class implements the Open/Closed Principle - new tab types can be
 * registered without modifying existing code.
 */
public final class TabContentLoaderRegistry {

    private final Map<String, TabContentLoader> loaders;

    /**
     * Creates a new tab loader registry.
     */
    public TabContentLoaderRegistry() {
        this.loaders = new HashMap<>();
    }

    /**
     * Registers a tab content loader for a specific tab name.
     *
     * @param tabName the name of the tab (case-insensitive)
     * @param loader the loader strategy for this tab type
     * @throws IllegalArgumentException if tabName or loader is null, or if tabName is already registered
     */
    public void registerLoader(String tabName, TabContentLoader loader) {
        Objects.requireNonNull(tabName, "Tab name cannot be null");
        Objects.requireNonNull(loader, "Loader cannot be null");

        String normalizedName = tabName.toLowerCase().trim();

        if (loaders.containsKey(normalizedName)) {
            throw new IllegalArgumentException("Loader already registered for tab: " + tabName);
        }

        loaders.put(normalizedName, loader);
    }

    /**
     * Loads content for a tab using the registered loader.
     *
     * @param tabName the name of the tab
     * @param tab the tab component
     * @param group the group context
     * @return true if a loader was found and executed, false otherwise
     */
    public boolean loadContent(String tabName, Tab tab, Group group) {
        Objects.requireNonNull(tabName, "Tab name cannot be null");
        Objects.requireNonNull(tab, "Tab cannot be null");
        Objects.requireNonNull(group, "Group cannot be null");

        String normalizedName = tabName.toLowerCase().trim();
        TabContentLoader loader = loaders.get(normalizedName);

        if (loader == null) {
            System.err.println("No loader registered for tab: " + tabName);
            return false;
        }

        try {
            loader.loadContent(tab, group);
            return true;
        } catch (Exception e) {
            System.err.println("Error loading content for tab '" + tabName + "': " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Checks if a loader is registered for a tab name.
     *
     * @param tabName the tab name to check
     * @return true if a loader is registered
     */
    public boolean hasLoader(String tabName) {
        if (tabName == null) return false;
        return loaders.containsKey(tabName.toLowerCase().trim());
    }

    /**
     * Gets the number of registered loaders.
     *
     * @return the count of registered loaders
     */
    public int getLoaderCount() {
        return loaders.size();
    }

    /**
     * Clears all registered loaders.
     */
    public void clear() {
        loaders.clear();
    }
}
