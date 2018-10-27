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
 * Created by nates on 10/21/2018.
 */
@RunWith(MockitoJUnitRunner.class)
public class PasswordUtilTest {

    @Mock
    Context mMockContext; //context mock required since this PasswordUtil pulls string resources
    @Before
    public void Setup() {
        when(mMockContext.getString(R.string.settings_password_not_matching)).thenReturn("Wrong or missing password");
        when(mMockContext.getString(R.string.settings_password_question)).thenReturn("Question");
        when(mMockContext.getString(R.string.settings_answer_not_matching)).thenReturn("Answer wrong or missing");
    }
    @Test
    public void PasswordValidation_Valid() {
        //Arrange
        PasswordUtil pwutil = new PasswordUtil(mMockContext);
        //Act
        String result = pwutil.PasswordValidation("pqlamz");
        //Assert
        assertNull(result);
    }
    @Test
    public void PasswordValidation_Invalid_Null() {
        //Arrange
        PasswordUtil pwutil = new PasswordUtil(mMockContext);
        //Act
        String result = pwutil.PasswordValidation(null);
        //Assert
        assertEquals(result,"Wrong or missing password");
    }
    @Test
    public void PasswordValidation_Invalid_Empty() {
        //Arrange
        PasswordUtil pwutil = new PasswordUtil(mMockContext);
        //Act
        String result = pwutil.PasswordValidation("");
        //Assert
        assertEquals(result,"Wrong or missing password");
    }

}
