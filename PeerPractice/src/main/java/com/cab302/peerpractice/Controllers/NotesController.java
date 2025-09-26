package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.AppContext;
import com.cab302.peerpractice.Model.Chapter;
import com.cab302.peerpractice.Model.Group;
import com.cab302.peerpractice.Model.Note;
import com.cab302.peerpractice.Model.NotesManager;
import com.cab302.peerpractice.Navigation;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;

import java.util.List;
import java.util.Optional;


public class NotesController extends BaseController{

    @FXML private SplitPane rootSplit;
    @FXML private ListView<Note> notesList;
    @FXML private ListView<Chapter> chaptersList;

    private NotesManager notesManager;
    private Group group;
    private Note currentNote;

    public NotesController(AppContext ctx, Navigation nav) {
        super(ctx, nav);
        notesManager = ctx.getNotesManager();
    }

    public void setGroup(Group group){
        this.group = group;
        updateNotesView();
    }

    @FXML
    public void initialize(){

        Platform.runLater(() -> {
            notesList.maxWidthProperty().bind(rootSplit.widthProperty().multiply(1.0/6.0));
            notesList.minWidthProperty().bind(rootSplit.widthProperty().multiply(1.0/6.0));
            chaptersList.maxWidthProperty().bind(rootSplit.widthProperty().multiply(1.0/6.0));
            chaptersList.minWidthProperty().bind(rootSplit.widthProperty().multiply(1.0/6.0));
        });

        /**
         * NotesList functionality
         */
        notesList.setCellFactory(listView -> new ListCell<>() {
            private final Button addButton = new Button("+");
            {
                addButton.setOnAction(e -> {
                    TextInputDialog dialog = new TextInputDialog();
                    dialog.setTitle("New Note");
                    dialog.setHeaderText("Create a new note");
                    dialog.setContentText("Enter note name:");

                    dialog.showAndWait().ifPresent(name -> {
                        String noteID = notesManager.addNote(name, group.getID());
                        Note newNote = ctx.getNotesDAO().getNote(noteID);
                        var items = notesList.getItems();
                        int insertIndex = Math.max(0, items.size() - 1);
                        items.add(insertIndex,newNote);
                    });
                });
            }

            @Override
            protected void updateItem(Note note, boolean empty) {
                super.updateItem(note, empty);

                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else if (note == null) {
                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    setAlignment(Pos.CENTER);
                    setText(null);
                    addButton.setDisable(group == null);
                    setGraphic(addButton);
                } else {
                    setGraphic(null);
                    setContentDisplay(ContentDisplay.TEXT_ONLY);
                    setAlignment(Pos.CENTER_LEFT);
                    setText(note.getName());
                }
            }
        });

        /**
         * Chapters List functionality
         */
        chaptersList.setCellFactory(listView -> new ListCell<>(){

            private final Button addButton = new Button("+");
            {
                addButton.setOnAction(e -> {
                    if(currentNote == null) return;
                    TextInputDialog dialog = new TextInputDialog();
                    dialog.setTitle("New Chapter");
                    dialog.setHeaderText("Create a new chapter");
                    dialog.setContentText("Enter chapter name:");

                    dialog.showAndWait().ifPresent(name -> {
                        String chapterID = notesManager.addChapter(currentNote.getID(),name);
                        Chapter newChapter = ctx.getNotesDAO().getChapter(chapterID);
                        System.out.println(newChapter.getID());
                        var items = chaptersList.getItems();
                        int insertIndex = Math.max(0, items.size() - 1);
                        items.add(insertIndex,newChapter);
                        chaptersList.getSelectionModel().select(newChapter);
                    });
                });
            }

            @Override
            protected void updateItem(Chapter ch, boolean empty) {
                super.updateItem(ch, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                    setContentDisplay(ContentDisplay.TEXT_ONLY);
                    setAlignment(Pos.CENTER_LEFT);
                    return;
                }
                if (ch == null) {
                    setText(null);
                    addButton.setDisable(currentNote == null);
                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    setAlignment(Pos.CENTER);
                    setGraphic(addButton);
                } else {
                    setGraphic(null);
                    setContentDisplay(ContentDisplay.TEXT_ONLY);
                    setAlignment(Pos.CENTER_LEFT);
                    setText(ch.getName());
                }
            }
        });

        notesList.getSelectionModel().selectedItemProperty().addListener((obs,oldNote,newNote) -> {
            if(newNote != null){
                currentNote = newNote;
                updateChaptersView(newNote);
            }
        });

    }

    private void updateChaptersView(Note note) {
        var items = FXCollections.<Chapter>observableArrayList();
        items.addAll(notesManager.getChapters(note.getID()));
        items.add(null);
        chaptersList.setItems(items);
    }

    private void updateNotesView(){
        if(group == null){
            notesList.setItems(FXCollections.observableArrayList());
            return;
        }
        var items = FXCollections.<Note>observableArrayList();
        List<Note> notes = notesManager.getNotes(group.getID());
        items.addAll(notes);
        items.add(null);
        notesList.setItems(items);

        if(!notes.isEmpty()){
            notesList.getSelectionModel().selectFirst();
        }
        else{
            clearChapters();
        }
    }

    private void clearChapters(){
        chaptersList.setItems(FXCollections.observableArrayList());
    }

}
