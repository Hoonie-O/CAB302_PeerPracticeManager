package com.cab302.peerpractice.Model.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Note {

    private final int groupID;
    private String ID;
    private String name;
    private ArrayList<String> chapters;

    public Note(String name, int groupID) {
        this.name = name;
        this.groupID = groupID;
        chapters = new ArrayList<String>();
    }

    //Getters
    public String getName(){return name;}
    public int getGroup(){return groupID;}
    public String getID(){return ID;}
    public List<String> getChapters(){return Collections.unmodifiableList(chapters);}

    //Setters
    public void setName(String name){
        this.name = name;
    }
    public void setID(String ID){
        this.ID = ID;
    }
    public void addChapter(String chapterID){
        chapters.add(chapterID);
    }


}
