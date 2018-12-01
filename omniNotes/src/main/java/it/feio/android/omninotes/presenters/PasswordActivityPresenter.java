package it.feio.android.omninotes.presenters;

import android.content.SharedPreferences;

import it.feio.android.omninotes.models.Password;

/**
 * Created by nates on 12/1/2018.
 */

public class PasswordActivityPresenter {
    private PasswordActivityView view;
    private Password model;

    public PasswordActivityPresenter(PasswordActivityView view) {
        this.model = new Password();
        this.view = view;
    }

    public void updatePasswordCheck(String passwordCheck) {
        this.model.setPasswordCheck(passwordCheck);
    }
    public void updatePassword(String password) {
        this.model.setPassword(password);
    }
    public void updateQuestion(String question) {
        this.model.setQuestion(question);
    }
    public void updateAnswer(String answer) {
        this.model.setAnswer(answer);
    }
    public void updateAnswerCheck(String answerCheck) {
        this.model.setAnswerCheck(answerCheck);
    }

    public void update(SharedPreferences prefs) {
        this.model.Update(this.view,prefs);
    }

}

