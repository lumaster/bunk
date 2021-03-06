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

package app.abhijit.iter.data;

import android.support.annotation.NonNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.commons.lang3.text.WordUtils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import app.abhijit.iter.exceptions.InvalidCredentialsException;
import app.abhijit.iter.exceptions.InvalidResponseException;
import app.abhijit.iter.models.Student;
import app.abhijit.iter.models.Subject;

/**
 * This class validates and parses the api response into a Student object.
 */
class ResponseParser {

    private JsonParser jsonParser;

    ResponseParser() {
        this.jsonParser = new JsonParser();
    }

    @NonNull
    String parseRegistrationId(@NonNull String registrationIdJson)
            throws InvalidResponseException {
        JsonObject registrationId;
        try {
            registrationId = this.jsonParser.parse(registrationIdJson).getAsJsonObject();
        } catch (Exception e) {
            throw new InvalidResponseException();
        }

        if (!registrationId.has("studentdata")) {
            throw new InvalidCredentialsException();
        }

        JsonArray studentData = registrationId.getAsJsonArray("studentdata");
        if (studentData.size() == 0) {
            throw new InvalidResponseException();
        }

        JsonObject[] years = new JsonObject[studentData.size()];
        for (int i = 0; i < studentData.size(); i++) {
            years[i] = studentData.get(i).getAsJsonObject();
            if (!years[i].has("REGISTRATIONID") || !years[i].has("REGISTRATIONDATEFROM")) {
                throw new InvalidResponseException();
            }
        }

        Arrays.sort(years, new Comparator<JsonObject>() {
            @Override
            public int compare(JsonObject o1, JsonObject o2) {
                return o1.get("REGISTRATIONDATEFROM").getAsString().compareTo(o2.get("REGISTRATIONDATEFROM").getAsString());
            }
        });
        return years[years.length - 1].get("REGISTRATIONID").getAsString();
    }

    @NonNull
    Student parseStudent(@NonNull String loginJson, @NonNull String attendanceJson)
            throws InvalidCredentialsException, InvalidResponseException {
        Student student = processLogin(loginJson);
        try {
            student.subjects.putAll(processAttendance(attendanceJson));
        } catch (InvalidResponseException ignored) { }

        return student;
    }

    private Student processLogin(String loginJson) {
        JsonObject login;
        try {
            login = this.jsonParser.parse(loginJson).getAsJsonObject();
        } catch (Exception e) {
            throw new InvalidResponseException();
        }

        if (!login.has("status") || !login.has("name")) {
            throw new InvalidResponseException();
        }

        if (!login.get("status").getAsString().equals("success")) {
            throw new InvalidCredentialsException();
        }

        Student student = new Student();
        student.name = WordUtils.capitalizeFully(login.get("name").getAsString());

        return student;
    }

    private HashMap<String, Subject> processAttendance(String attendanceJson) {
        JsonArray attendance;
        try {
            attendance = this.jsonParser.parse(attendanceJson)
                    .getAsJsonObject().get("griddata").getAsJsonArray();
        } catch (Exception e) {
            throw new InvalidResponseException();
        }

        HashMap<String, Subject> subjects = new HashMap<>();
        Pattern classesPattern = Pattern.compile("^(\\d+) / (\\d+)$");

        for (int i = 0; i < attendance.size(); i++) {
            JsonObject s = attendance.get(i).getAsJsonObject();
            if (!s.has("Latt") || !s.has("Patt")
                    || !s.has("subject") || !s.has("subjectcode")) {
                throw new InvalidResponseException();
            }

            Subject subject = new Subject();
            subject.name = s.get("subject").getAsString();
            subject.code = s.get("subjectcode").getAsString();
            subject.lastUpdated = new Date().getTime();

            Matcher theoryClassesMatcher = classesPattern.matcher(s.get("Latt").getAsString());
            if (theoryClassesMatcher.find()) {
                subject.theoryPresent = Integer.parseInt(theoryClassesMatcher.group(1));
                subject.theoryTotal = Integer.parseInt(theoryClassesMatcher.group(2));
            }

            Matcher labClassesMatcher = classesPattern.matcher(s.get("Patt").getAsString());
            if (labClassesMatcher.find()) {
                subject.labPresent = Integer.parseInt(labClassesMatcher.group(1));
                subject.labTotal = Integer.parseInt(labClassesMatcher.group(2));
            }

            subjects.put(subject.code, subject);
        }

        return subjects;
    }
}
