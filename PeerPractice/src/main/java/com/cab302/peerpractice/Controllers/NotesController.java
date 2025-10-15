package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.AppContext;
import com.cab302.peerpractice.Model.Entities.Chapter;
import com.cab302.peerpractice.Model.Entities.Group;
import com.cab302.peerpractice.Model.Entities.Note;
import com.cab302.peerpractice.Model.Managers.NotesManager;
import com.cab302.peerpractice.Navigation;
import javafx.application.Platform;
import javafx.collections.FXCollections;
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

/**
 * <hr>
 * Controller for managing collaborative note-taking functionality within groups.
 *
 * <p>This controller handles the creation, organization, and editing of notes
 * and chapters within study groups. It provides a Markdown-based editing interface
 * with real-time preview capabilities for collaborative note-taking.
 *
 * <p>Key features include:
 * <ul>
 *   <li>Hierarchical note and chapter organization</li>
 *   <li>Markdown content editing with live preview</li>
 *   <li>Group-based note sharing and collaboration</li>
 *   <li>WebView-based content rendering</li>
 *   <li>Integration with NotesManager for data persistence</li>
 * </ul>
 *
 * @see Note
 * @see Chapter
 * @see NotesManager
 * @see BaseController
 */
public class NotesController extends BaseController{

    /** <hr> Split pane container for the notes interface layout. */
    @FXML private SplitPane rootSplit;
    /** <hr> List view for displaying available notes. */
    @FXML private ListView<Note> notesList;
    /** <hr> List view for displaying chapters within selected notes. */
    @FXML private ListView<Chapter> chaptersList;
    /** <hr> Web view for rendering Markdown content preview. */
    @FXML private WebView contentWebView;
    /** <hr> Web engine for processing and displaying HTML content. */
    @FXML private WebEngine webEngine;
    /** <hr> Button for initiating content editing. */
    @FXML private Button editButton;
    /** <hr> Stack pane container for notes list section. */
    @FXML private StackPane notes;
    /** <hr> Stack pane container for chapters list section. */
    @FXML private StackPane chapters;
    /** <hr> Button for adding new notes. */
    @FXML private Button addNoteButton;
    /** <hr> Button for adding new chapters. */
    @FXML private Button addChapterButton;

    /**
     * <hr>
     * Manager for handling notes and chapters data operations.
     */
    private final NotesManager notesManager;
    /**
     * <hr>
     * The currently selected group for note operations.
     */
    private Group group;
    /**
     * <hr>
     * The currently selected note for chapter operations.
     */
    private Note currentNote;
    /**
     * <hr>
     * The currently selected chapter for content operations.
     */
    private Chapter currentChapter;

    /**
     * <hr>
     * Constructs a new NotesController with the specified context and navigation.
     *
     * @param ctx the application context providing access to user session and managers
     * @param nav the navigation controller for screen transitions
     */
    public NotesController(AppContext ctx, Navigation nav) {
        super(ctx, nav);
        notesManager = ctx.getNotesManager();
    }

    /**
     * <hr>
     * Sets the current group for note management and updates the notes view.
     *
     * @param group the group to set as current for note operations
     */
    public void setGroup(Group group){
        this.group = group;
        updateNotesView();
    }

    /**
     * <hr>
     * Initializes the controller after FXML loading is complete.
     *
     * <p>Sets up the notes interface including list view configurations,
     * web engine initialization, and event handlers for note and chapter
     * selection. Configures responsive layout constraints.
     */
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

    /**
     * <hr>
     * Updates the content view with rendered Markdown from the selected chapter.
     *
     * <p>Parses Markdown content from the chapter and renders it as HTML
     * in the WebView for preview. Clears the view if no chapter or empty content.
     *
     * @param chapter the chapter to display content from, or null to clear
     */
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

    /**
     * <hr>
     * Updates the chapters view with chapters from the selected note.
     *
     * <p>Retrieves and displays all chapters associated with the specified note
     * in the chapters list view.
     *
     * @param note the note to retrieve chapters for
     */
    private void updateChaptersView(Note note) {
        var items = FXCollections.<Chapter>observableArrayList();
        items.addAll(notesManager.getChapters(note.getID()));
        chaptersList.setItems(items);
    }

    /**
     * <hr>
     * Updates the notes view with notes from the current group.
     *
     * <p>Retrieves and displays all notes associated with the current group
     * in the notes list view. Clears the view if no group is selected.
     */
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

    /**
     * <hr>
     * Clears the chapters list view and related content.
     *
     * <p>Removes all chapters from the chapters list and clears any
     * displayed content in the preview pane.
     */
    private void clearChapters(){
        chaptersList.setItems(FXCollections.observableArrayList());
    }

    /**
     * <hr>
     * Handles content editing for the current chapter.
     *
     * <p>Opens a dialog for editing the Markdown content of the currently
     * selected chapter. Updates the content in the database and refreshes
     * the preview upon saving.
     */
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

    /**
     * <hr>
     * Handles creation of new notes.
     *
     * <p>Opens a dialog for creating new notes within the current group.
     * Adds the new note to the database and updates the notes list view.
     */
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

    /**
     * <hr>
     * Handles deletion of the current note.
     *
     * <p>Removes the currently selected note from the database and updates
     * the interface to reflect the change. Clears related chapters and content.
     */
    public void removeNote() {
        if(currentNote == null) return;
        notesManager.deleteNote(currentNote.getID());
        currentNote = null;
        updateNotesView();
        updateContentView(null);
    }

    /**
     * <hr>
     * Handles creation of new chapters.
     *
     * <p>Opens a dialog for creating new chapters within the current note.
     * Adds the new chapter to the database and updates the chapters list view.
     */
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

    /**
     * <hr>
     * Handles deletion of the current chapter.
     *
     * <p>Removes the currently selected chapter from the database and updates
     * the interface to reflect the change. Clears the content preview.
     */
    public void removeChapter() {
        if(currentChapter == null) return;
        notesManager.deleteChapter(currentChapter.getID());
        currentChapter = null;
        updateNotesView();
        updateContentView(null);
    }
}