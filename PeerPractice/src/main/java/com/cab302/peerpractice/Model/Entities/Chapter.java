package com.cab302.peerpractice.Model.Entities;

import java.util.Collections;
import java.util.List;

public class Chapter {

    private final String noteID;
    private String ID;
    private String name;
    private List<Attachment> attachments;
    private String content;

    public Chapter(String name,String noteID){
        this.noteID = noteID;
        this.name = name;
        this.attachments = new java.util.ArrayList<>();
        this.content = "";
    }

    //Getters
    public String getID(){return ID;}
    public String getName(){return name;}
    public String getNote(){return noteID;}
    public List<Attachment> getAttachments(){return Collections.unmodifiableList(attachments);}
    public String getContent(){return content;}

    //Setters
    public void setID(String id){ID = id;}
    public void setName(String name){this.name = name;}
    public void addAttachment(Attachment attachment){attachments.add(attachment);}
    public void setContent(String content){this.content = content;}



}
