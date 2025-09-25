package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.AppContext;
import com.cab302.peerpractice.Model.NotesManager;
import com.cab302.peerpractice.Navigation;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;


public class NotesController extends BaseController{

    @FXML private SplitPane rootSplit;
    @FXML private ListView<String> notesList;

    private NotesManager notesManager;

    public NotesController(AppContext ctx, Navigation nav) {
        super(ctx, nav);
        notesManager = ctx.getNotesManager();
    }

    @FXML
    public void initialize(){
        Platform.runLater(() -> rootSplit.setDividerPositions(1.0/6.0, 2.0/6.0));
        notesList.getItems().addAll(ctx.notes,)
    }




}
