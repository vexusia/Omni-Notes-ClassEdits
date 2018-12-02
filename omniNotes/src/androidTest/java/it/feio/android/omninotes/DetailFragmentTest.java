package it.feio.android.omninotes;

/**
 * Created by nates on 10/28/2018.
 */

import android.annotation.TargetApi;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Build;
import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.AppCompatImageView;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nhaarman.listviewanimations.itemmanipulation.DynamicListView;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;

import it.feio.android.omninotes.utils.Constants;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasErrorText;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static it.feio.android.omninotes.BaseEspressoTest.childAtPosition;
import static junit.framework.Assert.assertFalse;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class DetailFragmentTest {

    DetailFragment fragment = new DetailFragment();

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Before
    public void setupFragment() {
        mActivityRule.getActivity().getFragmentManager().beginTransaction();
    }

    @Test
    public void testAddingEmptyNote() {
        //add new note
        onView(withId(R.id.fab_expand_menu_button)).perform(click());

        //add new text note
        onView(withId(R.id.fab_note)).perform(click());

        //assert that the note did save and error message occured
        onView(withContentDescription("drawer open")).perform(click());
        onView(withId(R.id.crouton_handle)).check(matches(withClassName(is("android.widget.FrameLayout"))));

        //create new note
        onView(withId(R.id.fab_expand_menu_button)).perform(click());
        onView(withId(R.id.fab_note)).perform(click());

        //add title
        onView(withId(R.id.detail_title)).perform(typeText("Test"), closeSoftKeyboard());

        //save note
        onView(withContentDescription("drawer open")).perform(click());

        //select note again to edit
        onView(withText("Test")).perform(click());

        //remove title to make an empty note
        onView(withId(R.id.detail_title)).perform(clearText());

        //attempt to save and returns to main note view
        onView(withContentDescription("drawer open")).perform(click());
        ViewInteraction view = onView(withId(R.id.crouton_handle)).check(matches(withClassName(is("android.widget.FrameLayout"))));

        view.check(matches(isDisplayed()));

        //check that new note did not save and the old one was saved
        onView(withText("Test")).check(matches(isDisplayed()));

        //archive note
        onView(withText("Test")).perform(swipeRight());
        onView(withText("Test")).check(doesNotExist());
    }

    @Test
    public void testAddingNoteWithTitleContent() {
        //add new note
        onView(withId(R.id.fab_expand_menu_button)).perform(click());

        //add new text note
        onView(withId(R.id.fab_note)).perform(click());

        //add title
        onView(withId(R.id.detail_title)).perform(typeText("Test Title1"), closeSoftKeyboard());

        //save note
        onView(withContentDescription("drawer open")).perform(click());
        onView(withId(R.id.crouton_handle)).check(matches(withClassName(is("android.widget.FrameLayout"))));
        onView(withText(R.string.note_updated)).check(matches(isDisplayed()));

        // select note to edit
        onView(withText("Test Title1")).perform(click());

        //edit title and context
        onView(withId(R.id.detail_title)).perform(clearText()).perform(typeText("New Test Title"));
        onView(withId(R.id.detail_content)).perform(clearText()).perform(typeText("New note content"));

        //save note
        onView(withContentDescription("drawer open")).perform(click());

        //assert note exists
        onView(withText("New Test Title")).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));

        //archive note
        onView(withText("New Test Title")).perform(swipeRight());
        onView(withText("New Test Title")).check(doesNotExist());
    }

    @Test
    public void testAddingNoteWithNoteContent() {
        //add new note
        onView(withId(R.id.fab_expand_menu_button)).perform(click());

        //add new text note
        onView(withId(R.id.fab_note)).perform(click());

        //add content
        onView(withId(R.id.detail_content)).perform(typeText("This is note content"), closeSoftKeyboard());

        //save note
        onView(withContentDescription("drawer open")).perform(click());
        ViewInteraction view = onView(withId(R.id.crouton_handle)).check(matches(withClassName(is("android.widget.FrameLayout"))));

        view.check(matches(isDisplayed()));
    }

    @Test
    public void testAddingNoteWithContent() {
        //add new note
        onView(withId(R.id.fab_expand_menu_button)).perform(click());

        //add new text note
        onView(withId(R.id.fab_note)).perform(click());

        //add title and content
        onView(withId(R.id.detail_title)).perform(typeText("Test Title2"), closeSoftKeyboard());
        onView(withId(R.id.detail_content)).perform(typeText("This is note content"), closeSoftKeyboard());

        //save note
        onView(withContentDescription("drawer open")).perform(click());
        ViewInteraction view = onView(withId(R.id.crouton_handle)).check(matches(withClassName(is("android.widget.FrameLayout"))));

        view.check(matches(isDisplayed()));

        //archive note
        onView(withText("Test Title2")).perform(swipeRight());
        onView(withText("Test Title2")).check(doesNotExist());
    }

}
