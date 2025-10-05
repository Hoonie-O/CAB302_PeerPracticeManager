package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.AppContext;
import com.cab302.peerpractice.Model.Managers.Notifier;
import com.cab302.peerpractice.Navigation;
import javafx.scene.control.Label;
import javafx.stage.Popup;

public class PopupController extends BaseController{
    /**
     * <hr>
     * Constructs a new BaseController with the specified context and navigation.
     *
     * @param ctx the application context providing access to user session and managers
     * @param nav the navigation controller for screen transitions
     */
    protected PopupController(AppContext ctx, Navigation nav) {
        super(ctx, nav);
    }

    /**
     * Creates and shows a new popup on the main stage
     * @param msg The message displayed in the popup
     */
    public void createPopup(String msg) {
        Popup popup = new Popup();
        Label popupMsg = new Label(msg);
        popup.getContent().add(popupMsg);

        nav.displayPopup(popup);
    }
}
