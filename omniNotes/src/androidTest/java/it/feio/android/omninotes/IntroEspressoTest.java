package it.feio.android.omninotes;


import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class IntroEspressoTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    //clicks through the introduction slides
    public void IntroClickThrough(){
    onView(withId(R.id.next)).perform(click());
    onView(withId(R.id.next)).perform(click());
    onView(withId(R.id.next)).perform(click());
    onView(withId(R.id.next)).perform(click());
    onView(withId(R.id.next)).perform(click());
    onView(withId(R.id.done)).perform(click());
    }
}
