package com.cab302.peerpractice.Model.DAOs;

import com.cab302.peerpractice.Model.Entities.Attachment;
import com.cab302.peerpractice.Model.Entities.Chapter;
import com.cab302.peerpractice.Model.Entities.Note;

import java.util.List;

public interface INotesDAO {

    //Notes
    String addNote(Note note);
    void changeName(String noteID, String name);
    void deleteNote(String noteID);
    List<Note> getNotes(int groupID);
    Note getNote(String noteID);
    List<Note> getAllNotes();

    //Chapters
    String addChapter(String noteID, Chapter chapter);
    void updateChapter(String chapterID, String column, String value);
    void deleteChapter(String chapterID);
    List<Chapter>getChapters(String noteID);
    Chapter getChapter(String chapterID);

    //Attachments (Might or should be scoped to be a separate DAO as it should be used for chats as well lololol)
    String addAttachment(String chapterID, Attachment attachment);
    void updateAttachment(String attachmentID);
    void deleteAttachment(String attachmentID);
    void getAttachments(String chapterID);
    void getAttachment(String attachmentID);

}
