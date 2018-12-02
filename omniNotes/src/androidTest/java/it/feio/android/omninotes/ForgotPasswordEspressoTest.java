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
import static android.support.test.espresso.action.ViewActions.clearText;
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
public class ForgotPasswordEspressoTest {

    private String mPassword = "testingupastorm";
    private String mQuestion = "Is Chris Awesome?";
    private String mAnswer = "Yes, quite.";
    private String mWrongConfirmAnswer= "No, he isn't.";

    public void setPasswordFunction(){
        onView(withId(R.id.password)).check(matches(isFocusable()));
        onView(withId(R.id.password)).perform(clearText(), typeText(mPassword), closeSoftKeyboard());
        onView(withId(R.id.password)).check(matches(withText(mPassword)));

        onView(withId(R.id.password_check)).check(matches(isFocusable()));
        onView(withId(R.id.password_check)).perform(clearText(), typeText(mPassword), closeSoftKeyboard());
        onView(withId(R.id.password_check)).check(matches(withText(mPassword)));

        onView(withId(R.id.question)).check(matches(isFocusable()));
        onView(withId(R.id.question)).perform(clearText(), typeText(mQuestion), closeSoftKeyboard());
        onView(withId(R.id.question)).check(matches(withText(mQuestion)));

        onView(withId(R.id.answer)).check(matches(isFocusable()));
        onView(withId(R.id.answer)).perform(clearText(), typeText(mAnswer), closeSoftKeyboard());
        onView(withId(R.id.answer)).check(matches(withText(mAnswer)));

        onView(withId(R.id.answer_check)).check(matches(isFocusable()));
        onView(withId(R.id.answer_check)).perform(clearText(), typeText(mAnswer), closeSoftKeyboard());
        onView(withId(R.id.answer_check)).check(matches(withText(mAnswer)));

        onView(withId(R.id.password_confirm)).perform(
                new ViewAction() {
                    @Override
                    public Matcher<View> getConstraints() {
                        return ViewMatchers.isEnabled(); //no constraints
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

    public void navigateToPassword(){
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
    }

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void DeletePassword(){
        navigateToPassword();

        //clicks on the remove password button, notification appears that the password has not been set.
        onView(withId(R.id.password_forgotten)).perform(
                new ViewAction() {
                    @Override
                    public Matcher<View> getConstraints() {
                        return ViewMatchers.isEnabled(); //no constraints
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
        //sets the password
        setPasswordFunction();

        onView(withId(R.id.password_forgotten)).perform(
                new ViewAction() {
                    @Override
                    public Matcher<View> getConstraints() {
                        return ViewMatchers.isEnabled(); //no constraints
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

        onView(withId(R.id.reset_password_answer)).check(matches(isFocusable()));
        onView(withId(R.id.reset_password_answer)).perform(clearText(), typeText(mWrongConfirmAnswer), closeSoftKeyboard());
        onView(withId(R.id.reset_password_answer)).check(matches(withText(mWrongConfirmAnswer)));

        onView(withId(R.id.buttonDefaultPositive)).perform(
                new ViewAction() {
                    @Override
                    public Matcher<View> getConstraints() {
                        return ViewMatchers.isEnabled(); //no constraints
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

        onView(withId(R.id.reset_password_answer)).check(matches(isFocusable()));
        onView(withId(R.id.reset_password_answer)).perform(clearText(), typeText(mAnswer), closeSoftKeyboard());
        onView(withId(R.id.reset_password_answer)).check(matches(withText(mAnswer)));

        onView(withId(R.id.buttonDefaultPositive)).perform(
                new ViewAction() {
                    @Override
                    public Matcher<View> getConstraints() {
                        return ViewMatchers.isEnabled(); //no constraints
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
}
