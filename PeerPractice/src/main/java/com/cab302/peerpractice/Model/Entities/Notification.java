package com.cab302.peerpractice.Model.Entities;

public abstract class Notification {

   protected final User from;
   protected final String to;
   private String message;
   private boolean approved;
   private boolean denied;

   protected Notification(User from, String to ){
       this.from = from;
       this.to = to;
   }

   public User getFrom(){ return from;}

    public String getTo(){return  to;}

    public boolean isApproved(){return approved;}
    public void approve(){approved = true;}

    public boolean isDenied(){return denied;}
    public void deny(){denied = true;}

   public abstract String getMessage();


}
