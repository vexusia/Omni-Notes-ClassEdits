package it.feio.android.omninotes.models;
import android.content.SharedPreferences;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.presenters.PasswordActivityView;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.Security;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by nates on 12/1/2018.
 */

public class Password {
    private String passwordCheck;
    private String password;
    private String question;
    private String answer;
    private String answerCheck;

    private String getPasswordCheck() {
        return passwordCheck;
    }
    public void setPasswordCheck(String passwordCheck) {
        this.passwordCheck = passwordCheck;
    }
    private String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    private String getQuestion() {
        return question;
    }
    public void setQuestion(String question) {
        this.question = question;
    }
    private String getAnswer() {
        return answer;
    }
    public void setAnswer(String answer) {
        this.answer = answer;
    }
    private String getAnswerCheck() {
        return answerCheck;
    }
    public void setAnswerCheck(String answerCheck) {
        this.answerCheck = answerCheck;
    }
    public void Update(PasswordActivityView view, SharedPreferences prefs) {
        Observable
                .from(DbHelper.getInstance().getNotesWithLock(true))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(() -> prefs.edit()
                        .putString(Constants.PREF_PASSWORD, Security.md5(getPassword()))
                        .putString(Constants.PREF_PASSWORD_QUESTION, getQuestion())
                        .putString(Constants.PREF_PASSWORD_ANSWER, Security.md5(getAnswer()))
                        .commit())
                .doOnNext(note -> DbHelper.getInstance().updateNote(note, false))
                .doOnCompleted(() -> {
                    view.SaveCompleted();
                })
                .subscribe();
    }
}
