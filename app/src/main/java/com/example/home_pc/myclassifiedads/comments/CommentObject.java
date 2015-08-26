package com.example.home_pc.myclassifiedads.comments;

/**
 * Created by Ruchi on 2015-08-12.
 */
public class CommentObject {
   public String tableCategory,username,commentText,commentDate,userID,rating;
   public int adid,commentID;

    public CommentObject(String commentDate,String username,String commentText){
        this.commentDate=commentDate;
        this.username=username;
        this.commentText=commentText;
    }

    public CommentObject(int adid,String userID,String tableCategory){
        this.adid=adid;
        this.tableCategory=tableCategory;
        this.userID=userID;
    }

    public CommentObject(String tableCategory,String userID,int adid,String commentText){
        this.tableCategory=tableCategory;
        this.userID=userID;
        this.adid=adid;
        this.commentText=commentText;
    }

    public CommentObject(String username,int adid){
        this.adid=adid;
        this.username=username;
    }

}
