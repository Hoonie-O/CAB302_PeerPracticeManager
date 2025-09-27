package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.AppContext;
import com.cab302.peerpractice.Model.Chapter;
import com.cab302.peerpractice.Model.Group;
import com.cab302.peerpractice.Model.Note;
import com.cab302.peerpractice.Model.NotesManager;
import com.cab302.peerpractice.Navigation;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.util.List;



public class NotesController extends BaseController{

    @FXML private SplitPane rootSplit;
    @FXML private ListView<Note> notesList;
    @FXML private ListView<Chapter> chaptersList;
    @FXML private WebView contentWebView;
    @FXML private WebEngine webEngine;
    @FXML private Button editButton;
    @FXML private StackPane notes;
    @FXML private StackPane chapters;
    @FXML private Button addNoteButton;
    @FXML private Button addChapterButton;

    private NotesManager notesManager;
    private Group group;
    private Note currentNote;
    private Chapter currentChapter;

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
            notes.maxWidthProperty().bind(rootSplit.widthProperty().multiply(1.0/6.0));
            notes.minWidthProperty().bind(rootSplit.widthProperty().multiply(1.0/6.0));
            chapters.maxWidthProperty().bind(rootSplit.widthProperty().multiply(1.0/6.0));
            chapters.minWidthProperty().bind(rootSplit.widthProperty().multiply(1.0/6.0));
        });

        webEngine = contentWebView.getEngine();

        /**
         * NotesList functionality
         */
        notesList.setCellFactory(listView -> new ListCell<>() {

            @Override
            protected void updateItem(Note note, boolean empty) {
                super.updateItem(note, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else if (note == null) {
                    setText(null);
                    setGraphic(null);
                    addNoteButton.setDisable(group == null);
                } else {
                    setGraphic(null);
                    setContentDisplay(ContentDisplay.TEXT_ONLY);
                    setAlignment(Pos.CENTER_LEFT);
                    setText(note.getName());
                }
            }
        });

        //Every time a note is selected refresh the chapters view to reflect the chapters in that note
        notesList.getSelectionModel().selectedItemProperty().addListener((obs,oldNote,newNote) -> {
            if(newNote != null){
                currentNote = newNote;
                updateChaptersView(newNote);
                updateContentView(null);
            }
        });

        /**
         * Chapters List functionality
         */
        chaptersList.setCellFactory(listView -> new ListCell<>(){

            @Override
            protected void updateItem(Chapter ch, boolean empty) {
                super.updateItem(ch, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                }
                else if (ch == null) {
                    setText(null);
                    addChapterButton.setDisable(currentNote == null);
                } else {
                    setGraphic(null);
                    setContentDisplay(ContentDisplay.TEXT_ONLY);
                    setAlignment(Pos.CENTER_LEFT);
                    setText(ch.getName());
                }
            }
        });

        //Renders the note in markdown
        chaptersList.getSelectionModel().selectedItemProperty().addListener((obs,oldChapter,newChapter) -> {
            if(newChapter != null){
                currentChapter = newChapter;
                updateContentView(newChapter);
            }
        });

    }

    private void updateContentView(Chapter chapter){
        if(chapter == null || chapter.getContent() == null || chapter.getContent().isBlank()){
            webEngine.loadContent("","text/html");
            return;
        }
        Parser parser = Parser.builder().build();
        Node document = parser.parse(chapter.getContent());
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        webEngine.loadContent(renderer.render(document),"text/html");
    }

    private void updateChaptersView(Note note) {
        var items = FXCollections.<Chapter>observableArrayList();
        items.addAll(notesManager.getChapters(note.getID()));
        chaptersList.setItems(items);
    }

    private void updateNotesView(){
        if(group == null){
            notesList.setItems(FXCollections.observableArrayList());
            return;
        }
        List<Note> notes = notesManager.getNotes(group.getID());
        notesList.setItems(FXCollections.observableArrayList(notes));
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

    public void editContents() {
        if(currentNote == null) return;
        TextInputDialog dialog = new TextInputDialog();

        TextArea textArea = new TextArea();
        textArea.setPrefRowCount(12);
        textArea.setPrefColumnCount(40);
        textArea.setWrapText(true);
        textArea.setText(currentChapter.getContent());

        dialog.getDialogPane().setContent(textArea);

        dialog.setTitle("Markdown for " + currentChapter.getName());
        dialog.setHeaderText("Markdown for " + currentChapter.getName());
        dialog.setContentText(null);

        dialog.showAndWait().ifPresent(input -> {
            String content = textArea.getText();
            notesManager.changeContent(currentChapter.getID(),content);
            currentChapter.setContent(content);
            updateContentView(currentChapter);
        });

    }

    public void addNote() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Note");
        dialog.setHeaderText("Create a new note");
        dialog.setContentText("Enter note name:");

        dialog.showAndWait().ifPresent(name -> {
            String noteID = notesManager.addNote(name, group.getID());
            Note newNote = ctx.getNotesDAO().getNote(noteID);
            var items = notesList.getItems();
            items.add(newNote);
            notesList.getSelectionModel().select(newNote);
            currentNote = newNote;
        });
    }

    public void removeNote() {
        if(currentNote == null) return;
        notesManager.deleteNote(currentNote.getID());
        currentNote = null;
        updateNotesView();
        updateContentView(null);
    }

    public void addChapter() {
        if(currentNote == null) return;
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Chapter");
        dialog.setHeaderText("Create a new chapter");
        dialog.setContentText("Enter chapter name:");

        dialog.showAndWait().ifPresent(name -> {
            String chapterID = notesManager.addChapter(currentNote.getID(), name);
            Chapter newChapter = ctx.getNotesDAO().getChapter(chapterID);
            var items = chaptersList.getItems();
            items.add(newChapter);
            chaptersList.getSelectionModel().select(newChapter);
            currentChapter = newChapter;
        });
    }

    public void removeChapter() {
        if(currentChapter == null) return;
        notesManager.deleteChapter(currentChapter.getID());
        currentChapter = null;
        updateNotesView();
        updateContentView(null);
    }
}
