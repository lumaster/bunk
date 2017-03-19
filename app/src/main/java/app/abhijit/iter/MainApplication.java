/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Abhijit Parida <abhijitparida.me@gmail.com>
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

package app.abhijit.iter;

import android.app.Application;
import android.content.SharedPreferences;

import app.abhijit.iter.data.StudentDataSource;

@Deprecated
public class MainApplication extends Application {

    private boolean mLoggedIn = false;

    private StudentDataSource mStudentDataSource;

    public boolean isLoggedIn() {
        return mLoggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        mLoggedIn = loggedIn;
    }

    public StudentDataSource getStudentDataSource() {
        if (mStudentDataSource == null) {
            SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(StudentDataSource.LOCAL_STORE_NAME, MODE_PRIVATE);
            mStudentDataSource = new StudentDataSource(sharedPreferences);
        }

        return mStudentDataSource;
    }
}
