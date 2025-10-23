package com.cab302.peerpractice;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * Centralized store for UI-related state that needs to be shared between controllers.
 * <p>
 * The store exposes JavaFX {@link BooleanProperty} instances so controllers can
 * observe changes using property bindings or listeners. This enables views to
 * react automatically when another controller updates the menu or profile panel
 * visibility, providing a lightweight alternative to a full event bus.
 */
public class UiStateStore {

    private final BooleanProperty menuOpen = new SimpleBooleanProperty(this, "menuOpen", false);
    private final BooleanProperty profileOpen = new SimpleBooleanProperty(this, "profileOpen", false);

    /**
     * Returns the observable property tracking whether the navigation menu is open.
     *
     * @return the menu open property
     */
    public BooleanProperty menuOpenProperty() {
        return menuOpen;
    }

    /**
     * Gets the current value of {@link #menuOpenProperty()}.
     *
     * @return {@code true} if the menu should be displayed; otherwise {@code false}
     */
    public boolean isMenuOpen() {
        return menuOpen.get();
    }

    /**
     * Updates the value of {@link #menuOpenProperty()}.
     *
     * @param value {@code true} to mark the menu as open, {@code false} to mark it closed
     */
    public void setMenuOpen(boolean value) {
        menuOpen.set(value);
    }

    /**
     * Returns the observable property tracking whether the profile panel is open.
     *
     * @return the profile open property
     */
    public BooleanProperty profileOpenProperty() {
        return profileOpen;
    }

    /**
     * Gets the current value of {@link #profileOpenProperty()}.
     *
     * @return {@code true} if the profile panel should be displayed; otherwise {@code false}
     */
    public boolean isProfileOpen() {
        return profileOpen.get();
    }

    /**
     * Updates the value of {@link #profileOpenProperty()}.
     *
     * @param value {@code true} to mark the profile panel as open, {@code false} to mark it closed
     */
    public void setProfileOpen(boolean value) {
        profileOpen.set(value);
    }
}