package it.feio.android.omninotes.utils;

import android.content.Context;
import it.feio.android.omninotes.R;

/**
 * Created by nates on 10/21/2018.
 */

public class PasswordUtil {
    private Context mContext;

    public PasswordUtil(Context context) {
        mContext = context;
    }
    public boolean HasValidationError(String password, String passwordCheck,String question, String answer, String answerCheck) {
        return (!this.FieldOk(password) || !this.PasswordCheckOk(password,passwordCheck) || !this.FieldOk(question) ||
                    !this.FieldOk(answer) || !this.AnswerCheckOk(answer,answerCheck));
    }

    public String PasswordValidation(String password) {
        if (!this.FieldOk(password)) {
            return mContext.getString(R.string.settings_password_not_matching);
        }
        return null;
    }
    public String PasswordCheckValidation(String password, String passwordCheck) {
        if (!this.PasswordCheckOk(password, passwordCheck)) {
            return mContext.getString(R.string.settings_password_not_matching);
        }
        return null;
    }
    public String QuestionOkValidation(String question) {
        if (!this.FieldOk(question)) {
            return mContext.getString(R.string.settings_password_question);
        }
        return null;
    }

    public String AnswerOkValidation(String answer) {
        if (!this.FieldOk(answer)) {
            return mContext.getString(R.string.settings_answer_not_matching);
        }
        return null;
    }

    public String AnswerCheckOkValidation(String answer, String answerCheck) {
        if (!this.AnswerCheckOk(answer, answerCheck)) {
            return mContext.getString(R.string.settings_answer_not_matching);
        }
        return null;
    }

    public boolean PasswordCheckOk(String password, String passwordCheck) {
        return password!=null && passwordCheck.length() > 0 && password.equals(passwordCheck);
    }

    public boolean FieldOk(String value) {
        return value!=null && value.length() > 0;
    }

    public boolean AnswerCheckOk(String answer, String answerCheck) {
        return answerCheck.length() > 0 && answer.equals(answerCheck);
    }



}
