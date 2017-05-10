/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Shopify Inc.
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

package com.shopify.testify;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.action.ViewActions;
import android.view.ViewGroup;

import com.shopify.testify.exception.ScreenshotBaselineNotDefinedException;
import com.shopify.testify.exception.ScreenshotIsDifferentException;

import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

@SuppressWarnings("unused")
abstract class BaseScreenshotTest<T> {

    static final int NO_ID = -1;
    private static final long INFLATE_TIMEOUT_SECONDS = 5;
    private ViewModification viewModification;
    private EspressoActions espressoActions;
    @LayoutRes private int layoutId;
    private boolean hideSoftKeyboard = true;
    private Locale defaultLocale = null;

    BaseScreenshotTest(@LayoutRes int layoutId) {
        this.layoutId = layoutId;
    }

    protected abstract String getTestName();

    protected abstract Context getTestContext();

    protected abstract Activity getActivity();

    protected abstract T getThis();

    @SuppressWarnings("WeakerAccess")
    public T setViewModifications(ViewModification viewModification) {
        this.viewModification = viewModification;
        return getThis();
    }

    @SuppressWarnings("WeakerAccess")
    public T setEspressoActions(EspressoActions espressoActions) {
        this.espressoActions = espressoActions;
        return getThis();
    }

    public T setHideSoftKeyboard(boolean hideSoftKeyboard) {
        this.hideSoftKeyboard = hideSoftKeyboard;
        return getThis();
    }

    /**
     * Sets the default locale for this test case.
     * {@link #assertSame()} will reset the locale on completion.
     *
     * @param newLocale the new default locale
     * @return ScreenshotTest instance builder
     */
    public T setLocale(Locale newLocale) {
        defaultLocale = Locale.getDefault();
        LocaleHelper.setTestLocale(newLocale);
        return getThis();
    }

    @IdRes
    protected int getRootViewId() {
        return android.R.id.content;
    }

    private ViewGroup getRootView(Activity activity) {
        return (ViewGroup) activity.findViewById(getRootViewId());
    }

    private void initializeView(final Activity activity) throws Exception {
        final ViewGroup parentView = getRootView(activity);
        final CountDownLatch latch = new CountDownLatch(1);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (layoutId != NO_ID) {
                    activity.getLayoutInflater().inflate(layoutId, parentView, true);
                }
                if (viewModification != null) {
                    viewModification.modifyView(parentView);
                }
                latch.countDown();
            }
        });
        assertTrue(latch.await(INFLATE_TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }

    public void assertSame() throws Exception {
        try {
            final Activity activity = getActivity();
            initializeView(activity);

            if (espressoActions != null) {
                espressoActions.performEspressoActions();
            }

            if (hideSoftKeyboard) {
                Espresso.onView(withId(getRootViewId())).perform(ViewActions.closeSoftKeyboard());
            }

            final String testName = getTestName();
            final ScreenshotUtility screenshotUtility = new ScreenshotUtility();
            screenshotUtility.setLocale(defaultLocale != null ? Locale.getDefault() : null);
            Bitmap currentBitmap = screenshotUtility.createBitmapFromActivity(activity, testName);
            assertNotNull("Failed to capture bitmap from activity", currentBitmap);

            Bitmap baselineBitmap = screenshotUtility.loadBaselineBitmapForComparison(getTestContext(), testName);
            if (baselineBitmap == null) {
                throw new ScreenshotBaselineNotDefinedException(testName);
            }

            if (screenshotUtility.compareBitmaps(baselineBitmap, currentBitmap)) {
                // Delete the screenshot from the sdcard if it is identical to the current image
                assertTrue("Could not delete cached bitmap " + testName, screenshotUtility.deleteBitmap(activity, testName));
            } else {
                throw new ScreenshotIsDifferentException();
            }
        } finally {
            if (defaultLocale != null) {
                LocaleHelper.setTestLocale(defaultLocale);
                defaultLocale = null;
            }
        }
    }

    // TODO: Move to top-level to simplify import
    @SuppressWarnings("WeakerAccess")
    public interface ViewModification {

        void modifyView(ViewGroup rootView);
    }

    @SuppressWarnings("WeakerAccess")
    public interface EspressoActions {

        void performEspressoActions();
    }
}
