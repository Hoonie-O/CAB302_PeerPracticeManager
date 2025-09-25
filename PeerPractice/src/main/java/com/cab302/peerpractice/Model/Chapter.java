package com.cab302.peerpractice.Model;

import java.util.List;

public class Chapter {

    private final Note note;
    private int ID;
    private String name;
    private List<Attachment> attachments;
    private String content;

    public Chapter(String name,Note note){
        this.note = note;
        this.name = name;
    }





}
