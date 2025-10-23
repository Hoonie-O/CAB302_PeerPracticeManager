import com.cab302.peerpractice.UiStateStore;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UiStateStoreTest {

    @Test
    void defaultsAreClosed() {
        UiStateStore store = new UiStateStore();

        assertFalse(store.isMenuOpen(), "Menu should be closed by default");
        assertFalse(store.isProfileOpen(), "Profile should be closed by default");
    }

    @Test
    void menuStateIsObservable() {
        UiStateStore store = new UiStateStore();
        List<Boolean> observed = new ArrayList<>();
        store.menuOpenProperty().addListener((obs, oldValue, newValue) -> observed.add(newValue));

        store.setMenuOpen(true);
        store.setMenuOpen(false);

        assertEquals(List.of(true, false), observed);
    }

    @Test
    void profileStateIsObservable() {
        UiStateStore store = new UiStateStore();
        List<Boolean> observed = new ArrayList<>();
        store.profileOpenProperty().addListener((obs, oldValue, newValue) -> observed.add(newValue));

        store.setProfileOpen(true);
        store.setProfileOpen(false);

        assertEquals(List.of(true, false), observed);
    }

    @Test
    void statePersistsAcrossSharedReferences() {
        UiStateStore store = new UiStateStore();
        DummyController first = new DummyController(store);
        DummyController second = new DummyController(store);

        first.toggleMenu();
        assertTrue(second.isMenuOpen(), "Second controller should see menu open from first controller");

        second.toggleProfile();
        assertTrue(first.isProfileOpen(), "First controller should see profile open from second controller");

        second.toggleMenu();
        assertFalse(first.isMenuOpen(), "Toggling from second controller should close menu for first controller");
    }

    private static final class DummyController {
        private final UiStateStore store;

        private DummyController(UiStateStore store) {
            this.store = store;
        }

        void toggleMenu() {
            store.setMenuOpen(!store.isMenuOpen());
        }

        void toggleProfile() {
            store.setProfileOpen(!store.isProfileOpen());
        }

        boolean isMenuOpen() {
            return store.isMenuOpen();
        }

        boolean isProfileOpen() {
            return store.isProfileOpen();
        }
    }
}