package com.cab302.peerpractice.Model.daos;

import com.cab302.peerpractice.Model.entities.Attachment;
import com.cab302.peerpractice.Model.entities.Chapter;
import com.cab302.peerpractice.Model.entities.Note;

import java.util.List;

public interface INotesDAO {

    //Notes
    public String addNote(Note note);
    public void changeName(String noteID, String name);
    public void deleteNote(String noteID);
    public List<Note> getNotes(int groupID);
    public Note getNote(String noteID);
    public List<Note> getAllNotes();

    //Chapters
    public String addChapter(String noteID, Chapter chapter);
    public void updateChapter(String chapterID, String column, String value);
    public void deleteChapter(String chapterID);
    public List<Chapter>getChapters(String noteID);
    public Chapter getChapter(String chapterID);

    //Attachments (Might or should be scoped to be a separate DAO as it should be used for chats as well lololol)
    public String addAttachment(String chapterID, Attachment attachment);
    public void updateAttachment(String attachmentID);
    public void deleteAttachment(String attachmentID);
    public void getAttachments(String chapterID);
    public void getAttachment(String attachmentID);

}
