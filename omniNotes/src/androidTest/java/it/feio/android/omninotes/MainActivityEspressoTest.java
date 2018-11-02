package it.feio.android.omninotes;

import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isFocusable;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static it.feio.android.omninotes.BaseEspressoTest.childAtPosition;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class MainActivityEspressoTest {

    private String mPassword = "testingupastorm";
    private String mQuestion = "Is Chris Awesome?";
    private String mAnswer = "Yes, quite.";

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void SetNewPassword(){
        //clicks on the upper left hand menu icon, opens omni note menu
        onView(withContentDescription("drawer open")).perform(click());

        //clicks on the settings option, opens the settings menu
        onView(withId(R.id.settings_view)).perform(click());

        //clicks on the data option, opens the data menu
        DataInteraction linearLayout2 = onData(anything()).inAdapterView(allOf(withId(android.R.id.list),
                childAtPosition(withClassName(is("android.widget.FrameLayout")),0))).atPosition(1);
        linearLayout2.perform(click());

        //clicks on the password option, brings up the password form
        DataInteraction linearLayout3 = onData(anything()).inAdapterView(allOf(withId(android.R.id.list),
                childAtPosition(withClassName(is("android.widget.FrameLayout")),0))).atPosition(1);
        linearLayout3.perform(click());

        //checks if the EditText is focusable, types the password, then checks if the typed password matches the variable.
        onView(withId(R.id.password)).check(matches(isFocusable()));
        onView(withId(R.id.password)).perform(typeText(mPassword), closeSoftKeyboard());
        onView(withId(R.id.password)).check(matches(withText(mPassword)));

        //checks if the EditText is focusable, types the password confirmation, then checks if the typed password confirmation matches the variable.
        onView(withId(R.id.password_check)).check(matches(isFocusable()));
        onView(withId(R.id.password_check)).perform(typeText(mPassword), closeSoftKeyboard());
        onView(withId(R.id.password_check)).check(matches(withText(mPassword)));

        //checks if the EditText is focusable, types the security question, then checks if the typed security question matches the variable.
        onView(withId(R.id.question)).check(matches(isFocusable()));
        onView(withId(R.id.question)).perform(typeText(mQuestion), closeSoftKeyboard());
        onView(withId(R.id.question)).check(matches(withText(mQuestion)));

        //checks if the EditText is focusable, types the security question answer, then checks if the typed security question answer matches the variable.
        onView(withId(R.id.answer)).check(matches(isFocusable()));
        onView(withId(R.id.answer)).perform(typeText(mAnswer), closeSoftKeyboard());
        onView(withId(R.id.answer)).check(matches(withText(mAnswer)));

        //checks if the EditText is focusable, types the security question answer confirmation, then checks if the typed security question answer confirmation matches the variable.
        onView(withId(R.id.answer_check)).check(matches(isFocusable()));
        onView(withId(R.id.answer_check)).perform(typeText(mAnswer), closeSoftKeyboard());
        onView(withId(R.id.answer_check)).check(matches(withText(mAnswer)));

        //clicks on the OK button to submit.  The function allows clicking on the OK button when it is outside of the view window.  On smaller screens the OK button is not displayed and unable to be clicked on.
        onView(withId(R.id.password_confirm)).perform(
                new ViewAction() {
                    @Override
                    public Matcher<View> getConstraints() {
                        return ViewMatchers.isEnabled(); // no constraints, they are checked above
                    }

                    @Override
                    public String getDescription() {
                        return "click plus button";
                    }

                    @Override
                    public void perform(UiController uiController, View view) {
                        view.performClick();
                    }
                }
        );

    }

    @Test
    public void DeletePassword(){

    }
}
