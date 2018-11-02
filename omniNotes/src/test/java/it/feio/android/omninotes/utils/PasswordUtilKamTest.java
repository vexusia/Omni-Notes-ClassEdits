package it.feio.android.omninotes.utils;

import android.content.Context;
import android.test.InstrumentationTestCase;
import android.widget.EditText;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.Mockito.*;

import java.util.HashMap;

import it.feio.android.omninotes.BuildConfig;
import it.feio.android.omninotes.PasswordActivity;
import it.feio.android.omninotes.R;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Created by Kam on 10/27/2018.
 */
@RunWith(MockitoJUnitRunner.class)
public class PasswordUtilKamTest {

    @Mock
    Context mMockContext; //context mock required since this PasswordUtil pulls string resources
    @Before
    public void Setup() {
        when(mMockContext.getString(R.string.settings_password_not_matching)).thenReturn("Wrong or missing password");
        when(mMockContext.getString(R.string.settings_password_question)).thenReturn("Question");
        when(mMockContext.getString(R.string.settings_answer_not_matching)).thenReturn("Answer wrong or missing");
    }

    @Test
    public void HasValidationError_False () {
        PasswordUtil pwutil = new PasswordUtil(mMockContext);

        boolean result = pwutil.HasValidationError("pqlamz", "pqlamz", "what is this?", "test", "test");

        assertFalse(result);
    }

    @Test
    public void HasValidationError_FieldOkPwFalse () {
        PasswordUtil pwutil = new PasswordUtil(mMockContext);

        boolean result = pwutil.HasValidationError("", "pqlamz", "what is this?", "test", "test");

        assertTrue(result);
    }

    @Test
    public void HasValidationError_FieldOkQuestionFalse () {
        PasswordUtil pwutil = new PasswordUtil(mMockContext);

        boolean result = pwutil.HasValidationError("pqlamz", "pqlamz", "", "test", "test");

        assertTrue(result);
    }

    @Test
    public void HasValidationError_FieldOkAnswerFalse () {
        PasswordUtil pwutil = new PasswordUtil(mMockContext);

        boolean result = pwutil.HasValidationError("pqlamz", "pqlamz", "what is this?", "", "test");

        assertTrue(result);
    }

    @Test
    public void HasValidationError_FieldOkAnswerCheckFalse () {
        PasswordUtil pwutil = new PasswordUtil(mMockContext);

        boolean result = pwutil.HasValidationError("pqlamz", "pqlamz", "what is this?", "test", "testWrong");

        assertTrue(result);
    }

    @Test
    public void HasValidationError_PasswordOkFalse () {
        PasswordUtil pwutil = new PasswordUtil(mMockContext);

        boolean result = pwutil.HasValidationError("pqlam", "pqlamz", "what is this?", "test", "test");

        assertTrue(result);
    }

    @Test
    public void PasswordCheckValidation_Null() {
        PasswordUtil pwutil = new PasswordUtil(mMockContext);

        String result = pwutil.PasswordCheckValidation("pqlamz", "pqlamz");

        assertNull(result);
    }

    @Test
    public void PasswordCheckValidation_False() {
        PasswordUtil pwutil = new PasswordUtil(mMockContext);

        String result1 = pwutil.PasswordCheckValidation("pqla", "pqlamz");
        assertEquals(result1, "Wrong or missing password");

        String result2 = pwutil.PasswordCheckValidation(null, "pqlamz");
        assertEquals(result2, "Wrong or missing password");

        String result3 = pwutil.PasswordCheckValidation("", "pqlamz");
        assertEquals(result3, "Wrong or missing password");
    }

    @Test
    public void QuestionOkValidation_ReturnsNull() {
        PasswordUtil pwutil = new PasswordUtil(mMockContext);

        String result = pwutil.QuestionOkValidation("what is the meaning of life, the universe, and everything?");

        assertNull(result);
    }

    @Test
    public void QuestionOkValidation_ReturnsString() {
        PasswordUtil pwutil = new PasswordUtil(mMockContext);

        String result1 = pwutil.QuestionOkValidation("");
        assertEquals(result1, "Question");

        String result2 = pwutil.QuestionOkValidation(null);
        assertEquals(result2, "Question");
    }

    @Test
    public void AnswerOkValidation_ReturnsNull() {
        PasswordUtil pwutil = new PasswordUtil(mMockContext);

        String result = pwutil.AnswerOkValidation("42");

        assertNull(result);
    }

    @Test
    public void AnswerOkValidation_ReturnsString() {
        PasswordUtil pwutil = new PasswordUtil(mMockContext);

        String result1 = pwutil.AnswerOkValidation("");
        assertEquals(result1, "Answer wrong or missing");

        String result2 = pwutil.AnswerOkValidation(null);
        assertEquals(result2, "Answer wrong or missing");
    }

    @Test
    public void AnswerCheckOkValidation_ReturnsNull() {
        PasswordUtil pwutil = new PasswordUtil(mMockContext);

        String result = pwutil.AnswerCheckOkValidation("42", "42");

        assertNull(result);
    }

    @Test
    public void AnswerCheckOkValidation_ReturnsString() {
        PasswordUtil pwutil = new PasswordUtil(mMockContext);

        String result1 = pwutil.AnswerCheckOkValidation("42", "");
        assertEquals(result1, "Answer wrong or missing");

        String result2 = pwutil.AnswerCheckOkValidation("42", "43");
        assertEquals(result2, "Answer wrong or missing");
    }
}
