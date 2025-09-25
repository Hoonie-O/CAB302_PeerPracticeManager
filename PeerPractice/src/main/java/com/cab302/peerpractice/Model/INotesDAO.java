package com.cab302.peerpractice.Model;

import java.util.List;

public interface INotesDAO {

    //Notes
    public int addNote(Note note);
    public void updateNote(String noteID, String column, String value);
    public void deleteNote(String noteID);
    public List<Note> getNotes(String groupID);
    public Note getNote(String noteID);

    //Chapters
    public int addChapter(String noteID, Chapter chapter);
    public void updateChapter(String chapterID, String column, String value);
    public void deleteChapter(String chapterID);
    public List<Chapter>getChapters(String noteID);
    public Chapter getChapter(String chapterID);

    //Attachments (Might or should be scoped to be a separate DAO as it should be used for chats as well lololol)
    public int addAttachment(String chapterID, Attachment attachment);
    public void updateAttachment(String attachmentID);
    public void deleteAttachment(String attachmentID);
    public void getAttachments(String chapterID);
    public void getAttachment(String attachmentID);

}
