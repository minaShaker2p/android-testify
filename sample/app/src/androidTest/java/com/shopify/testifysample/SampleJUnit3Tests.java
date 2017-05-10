/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Shopify Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.shopify.testifysample;

import android.view.ViewGroup;
import android.widget.RadioButton;

import com.shopify.testify.ScreenshotTest;
import com.shopify.testify.ScreenshotTestCase;

import java.util.Locale;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public class SampleJUnit3Tests extends ScreenshotTestCase<TestHarnessActivity> {

    private Locale defaultLocale;

    public SampleJUnit3Tests() {
        super(TestHarnessActivity.class, R.id.component_placeholder);
    }

    public void testDefault() throws Exception {
        new ScreenshotTest(this, R.layout.test_sample).assertSame();
    }

    public void testMainActivity() throws Exception {
        new ScreenshotTest(this, R.layout.activity_main).assertSame();
    }

    public void testFrenchMainActivity() throws Exception {
        new ScreenshotTest(this, R.layout.activity_main)
                .setLocale(Locale.FRANCE)
                .assertSame();
    }

    public void testEspressoActions() throws Exception {
        new ScreenshotTest(this, R.layout.test_sample)
                .setEspressoActions(new ScreenshotTest.EspressoActions() {
                    @Override
                    public void performEspressoActions() {
                        onView(withId(R.id.checkBox)).perform(click());
                    }
                })
                .assertSame();
    }

    public void testViewModifications() throws Exception {
        new ScreenshotTest(this, R.layout.test_sample)
                .setViewModifications(new ScreenshotTest.ViewModification() {
                    @Override
                    public void modifyView(ViewGroup rootView) {
                        ((RadioButton) rootView.findViewById(R.id.radioButton)).setChecked(true);
                    }
                })
                .assertSame();
    }
}