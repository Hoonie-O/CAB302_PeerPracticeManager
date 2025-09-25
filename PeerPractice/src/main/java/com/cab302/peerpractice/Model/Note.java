package com.cab302.peerpractice.Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Note {

    private final Group group;
    private int ID;
    private String name;
    private ArrayList<Chapter> chapters;

    public Note(String name, Group group) {
        this.name = name;
        this.group = group;
    }

    //Getters
    public String getName(){return name;}
    public Group getGroup(){return group;}
    public int getID(){return ID;}
    public List<Chapter> getChapters(){return Collections.unmodifiableList(chapters);}

    //Setters
    public void setName(String name){
        this.name = name;
    }

    public void setID(int ID){
        this.ID = ID;
    }

    public void addChapter(Chapter chapter){
        chapters.add(chapter);
    }



}
