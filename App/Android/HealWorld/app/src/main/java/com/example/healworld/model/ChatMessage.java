package com.example.healworld.model;

import com.stfalcon.chatkit.commons.models.IMessage;

import java.util.Date;

/*
 * Created by troy379 on 04.04.17.
 */
public class ChatMessage implements IMessage {

    private String id;
    private String text;
    private Date createdAt;
    private User user;
    private String correspondingQuestion;
    private String questionContext;

    public ChatMessage(String id, User user, String text) {
        this(id, user, text, "null", "null");
    }

    public ChatMessage(String id, User user, String text, String question, String context){
        this(id, user, text, "null", "null", new Date());
    }

    public ChatMessage(String id, User user, String text, String question, String context, Date createdAt){
        this.id = id;
        this.text = text;
        this.user = user;
        this.createdAt = createdAt;
        this.correspondingQuestion = question;
        this.questionContext = context;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public Date getCreatedAt() {
        return createdAt;
    }

    @Override
    public User getUser() {
        return this.user;
    }

    public String getCorrespondingQuestion(){
        return this.correspondingQuestion;
    }

    public String getQuestionContext(){
        return this.questionContext;
    }
}
