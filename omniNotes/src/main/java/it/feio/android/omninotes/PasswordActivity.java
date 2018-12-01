/*
 * Copyright (C) 2018 Federico Iosue (federico.iosue@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.feio.android.omninotes;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.EditText;
import com.afollestad.materialdialogs.MaterialDialog;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.LifecycleCallback;
import it.feio.android.omninotes.async.bus.PasswordRemovedEvent;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.models.ONStyle;
import it.feio.android.omninotes.presenters.PasswordActivityPresenter;
import it.feio.android.omninotes.presenters.PasswordActivityView;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.PasswordHelper;
import it.feio.android.omninotes.utils.PasswordUtil;
import it.feio.android.omninotes.utils.Security;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class PasswordActivity extends BaseActivity implements PasswordActivityView {

    private ViewGroup crouton_handle;
    public EditText passwordCheck;
	public EditText password;
	public EditText question;
	public EditText answer;
	public EditText answerCheck;
    private PasswordActivity mActivity;
    private PasswordUtil pwUtil;
	private PasswordActivityPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pwUtil =new PasswordUtil(this.getBaseContext());
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int screenWidth = (int) (metrics.widthPixels * 0.80);
        int screenHeight = (int) (metrics.heightPixels * 0.80);
        setContentView(R.layout.activity_password);
        getWindow().setLayout(screenWidth, screenHeight);
        mActivity = this;
        setActionBarTitle(getString(R.string.title_activity_password));
		presenter = new PasswordActivityPresenter(this);
        initViews();
        initModelBinding();
    }


    private void initViews() {
        crouton_handle = (ViewGroup) findViewById(R.id.crouton_handle);
        password = (EditText) findViewById(R.id.password);
        passwordCheck = (EditText) findViewById(R.id.password_check);
        question = (EditText) findViewById(R.id.question);
        answer = (EditText) findViewById(R.id.answer);
        answerCheck = (EditText) findViewById(R.id.answer_check);

        findViewById(R.id.password_remove).setOnClickListener(v -> {
			if (prefs.getString(Constants.PREF_PASSWORD, null) != null) {
				PasswordHelper.requestPassword(mActivity, passwordConfirmed -> {
					if (passwordConfirmed) {
						updatePassword(null, null, null);
					}
				});
			} else {
				Crouton.makeText(mActivity, R.string.password_not_set, ONStyle.WARN, crouton_handle).show();
			}
		});

        findViewById(R.id.password_confirm).setOnClickListener(v -> {
			this.PasswordSubmit();
		});

        findViewById(R.id.password_forgotten).setOnClickListener(v -> {
			if (prefs.getString(Constants.PREF_PASSWORD, "").length() == 0) {
				Crouton.makeText(mActivity, R.string.password_not_set, ONStyle.WARN, crouton_handle).show();
				return;
			}
			PasswordHelper.resetPassword(this);
		});
    }
	//Add listeners to assist our presenter in binding values to our model
    private void initModelBinding() {
		passwordCheck.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				presenter.updatePasswordCheck(s.toString());
			}
			@Override
			public void afterTextChanged(Editable s) {

			}
		});
		password.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				presenter.updatePassword(s.toString());
			}
			@Override
			public void afterTextChanged(Editable s) {

			}
		});
		question.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				presenter.updateQuestion(s.toString());
			}
			@Override
			public void afterTextChanged(Editable s) {

			}
		});
		answer.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				presenter.updateAnswer(s.toString());
			}
			@Override
			public void afterTextChanged(Editable s) {

			}
		});
		answerCheck.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				presenter.updateAnswerCheck(s.toString());
			}
			@Override
			public void afterTextChanged(Editable s) {

			}
		});
	}
    public void PasswordSubmit() {
		final String passwordText = password.getText().toString();
		final String passwordCheckText = passwordCheck.getText().toString();
		final String questionText = question.getText().toString();
		final String answerText = answer.getText().toString();
		final String answerCheckText = answerCheck.getText().toString();
		password.setError(pwUtil.PasswordValidation(passwordText));
		passwordCheck.setError(pwUtil.PasswordCheckValidation(passwordText, passwordCheckText));
		question.setError(pwUtil.QuestionOkValidation(questionText));
		answer.setError(pwUtil.AnswerOkValidation(answerText));
		answerCheck.setError(pwUtil.AnswerCheckOkValidation(answerText, answerCheckText));
		if (!pwUtil.HasValidationError(passwordText,passwordCheckText, questionText, answerText, answerCheckText)) {
			if (prefs.getString(Constants.PREF_PASSWORD, null) != null) {
				PasswordHelper.requestPassword(mActivity, passwordConfirmed -> {
					if (passwordConfirmed) {
						updatePassword(passwordText, questionText, answerText);
					}
				});
			} else {
				updatePassword(passwordText, questionText, answerText);
			}
		}
	}
	public void onEvent(PasswordRemovedEvent passwordRemovedEvent) {
			passwordCheck.setText("");
			password.setText("");
			question.setText("");
			answer.setText("");
			answerCheck.setText("");
			Crouton crouton = Crouton.makeText(mActivity, R.string.password_successfully_removed, ONStyle
							.ALERT,
					crouton_handle);
			crouton.setLifecycleCallback(new LifecycleCallback() {
				@Override
				public void onDisplayed() {
					// Does nothing!
				}


				@Override
				public void onRemoved() {
					onBackPressed();
				}
			});
			crouton.show();
	}


	@SuppressLint("CommitPrefEdits")
	private void updatePassword(String passwordText, String questionText, String answerText) {
		if (passwordText == null) {
			if (prefs.getString(Constants.PREF_PASSWORD, "").length() == 0) {
				Crouton.makeText(mActivity, R.string.password_not_set, ONStyle.WARN, crouton_handle).show();
				return;
			}
			new MaterialDialog.Builder(mActivity)
					.content(R.string.agree_unlocking_all_notes)
					.positiveText(R.string.ok)
					.callback(new MaterialDialog.ButtonCallback() {
						@Override
						public void onPositive(MaterialDialog materialDialog) {
							PasswordHelper.removePassword();
						}
					}).build().show();
		} else if (passwordText.length() == 0) {
			Crouton.makeText(mActivity, R.string.empty_password, ONStyle.WARN, crouton_handle).show();
		} else {
			presenter.update(prefs);
		}
	}

	public void SaveCompleted() {
		Crouton crouton = Crouton.makeText(mActivity, R.string.password_successfully_changed, ONStyle
                        .CONFIRM, crouton_handle);
		crouton.setLifecycleCallback(new LifecycleCallback() {
            @Override
            public void onDisplayed() {
                // Does nothing!
            }


            @Override
            public void onRemoved() {
                onBackPressed();
            }
        });
		crouton.show();
	}

	@Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        finish();
    }

}
