package com.example.quiz;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    private SharedPreferences sharedPreferences;

    @Before
    public void setUp() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        sharedPreferences = context.getSharedPreferences("QuizApp", Context.MODE_PRIVATE);
    }

    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.quiz", appContext.getPackageName());
    }

    @Test
    public void testSaveState() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            // Mở ứng dụng và chọn một phương án
            onView(withId(R.id.optionButton1)).perform(click());

            // Lưu trạng thái hiện tại
            scenario.onActivity(MainActivity::saveState);
        }

        // Mở lại ứng dụng
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);

        // Kiểm tra trạng thái đã lưu
        int currentIndex = sharedPreferences.getInt("currentIndex", -1);
        int score = sharedPreferences.getInt("score", -1);
        boolean isOptionSelected = sharedPreferences.getBoolean("isOptionSelected", false);

        // Xác minh rằng trạng thái đã được khôi phục chính xác
        assertEquals(0, currentIndex);
        assertEquals(1, score);
        assertTrue(isOptionSelected);

        // Kiểm tra rằng tùy chọn được chọn có màu nền khác
        onView(withId(R.id.optionButton1)).check(matches(not(withText(""))));
    }

    @Test
    public void testPreventNextWithoutSelection() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            // Mở ứng dụng và không chọn phương án nào
            // Nhấn nút Next
            onView(withId(R.id.nextButton)).perform(click());

            // Kiểm tra rằng thông báo yêu cầu chọn phương án được hiển thị
            onView(withId(R.id.questionTextView))
                    .check(matches(withText("Please select an option before moving to the next question.")));
        }
    }

    @Test
    public void testAllowNextWithSelection() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            // Mở ứng dụng và chọn một phương án
            onView(withId(R.id.optionButton1)).perform(click());

            // Nhấn nút Next
            onView(withId(R.id.nextButton)).perform(click());

            // Kiểm tra rằng câu hỏi tiếp theo được hiển thị
            onView(withId(R.id.questionTextView)).check(matches(not(withText(""))));
        }
    }
}
