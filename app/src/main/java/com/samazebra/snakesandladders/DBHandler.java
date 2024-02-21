package com.samazebra.snakesandladders;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

public class DBHandler extends SQLiteOpenHelper
{
    // All Member Variables and Variables for Table/Column Names
    public static final String DB_NAME = "dbQuestions";
    public static final int DB_VERSION = 1;
    public static final String QUESTION_TABLE_NAME = "questions";
    public static final String QUESTION_NAME_COL = "question_name";
    public static final String OPTION_NAME_COL = "option_name";
    public static final String SUBJECT_NAME_COL = "subject_name";
    public static final String TOPIC_NAME_COL = "topic";
    public static final String NUM_CORRECT_COL = "number_correct";
    public static final String NUM_TOTAL_COL = "total_number";
    public static final String ANSWER_COL = "answer";
    public static final String OPTIONS_TABLE_NAME = "options";
    public static final String SUBJECTS_TABLE_NAME = "subjects";
    public static final String TOPICS_TABLE_NAME = "topics";
    public static final String QUESTIONS_ID_COL = "question_id";
    public static final String OPTIONS_ID_COL = "option_id";
    public static final String SUBJECT_ID_COL = "subject_id";
    public static final String TOPIC_ID_COL = "topic_id";

    // Constructor for Database
    public DBHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // Creates the Database and Tables
    @Override
    public void onCreate(SQLiteDatabase db)
    {
        // on below line we are creating
        // an sqlite query and we are
        // setting our column names
        // along with their data types.
        String query = "CREATE TABLE " + QUESTION_TABLE_NAME + " ("
                + QUESTIONS_ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + QUESTION_NAME_COL + " TEXT,"
                + TOPIC_ID_COL + " INTEGER,"
                + ANSWER_COL + " INTEGER,"
                + NUM_CORRECT_COL + " INTEGER,"
                + NUM_TOTAL_COL + " INTEGER)";

        String query2 = "CREATE TABLE " + OPTIONS_TABLE_NAME + " ("
                + OPTIONS_ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + OPTION_NAME_COL + " TEXT,"
                + QUESTIONS_ID_COL + " INTEGER)";

        String query3 = "CREATE TABLE " + SUBJECTS_TABLE_NAME + " ("
                + SUBJECT_ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + SUBJECT_NAME_COL + " TEXT)";

        String query4 = "CREATE TABLE " + TOPICS_TABLE_NAME + " ("
                + TOPIC_ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + TOPIC_NAME_COL + " TEXT,"
                + SUBJECT_ID_COL + " INTEGER)";

        // at last we are calling a exec sql
        // method to execute above sql query
        db.execSQL(query);
        db.execSQL(query2);
        db.execSQL(query3);
        db.execSQL(query4);
    }


    // This method is used to add a new course to our SQLite database.
    public void addNew(String name, String subject, String topic, String[] options, int correctOption) {
        // Get a writable instance of the SQLiteDatabase.
        SQLiteDatabase db = this.getWritableDatabase();

        // Create a ContentValues object to hold values for the Question table.
        ContentValues questionValues = new ContentValues();
        // Add question name, initial number of correct answers and total attempts to ContentValues.
        questionValues.put(QUESTION_NAME_COL, name);
        questionValues.put(NUM_CORRECT_COL, 0);
        questionValues.put(NUM_TOTAL_COL, 0);

        // Check if the subject exists in the database and get its ID.
        int subjectID = checkSubject(subject);
        // Check if the topic exists under the subject in the database and get its ID.
        int topicID = checkTopic(subjectID, topic);

        // Add the Topic ID to the ContentValues.
        questionValues.put(TOPIC_ID_COL, topicID);

        // Insert the new question into the Question table and store the newly generated ID.
        long questionId = db.insert(QUESTION_TABLE_NAME, null, questionValues);

        // Variable to store the ID of the correct option.
        int correctOptionId = -1;

        // Loop through the array of options and add them to the Options table.
        for (int i = 0; i < options.length; i++) {
            ContentValues optionValues = new ContentValues();
            // Add option name and associate it with the question ID.
            optionValues.put(OPTION_NAME_COL, options[i]);
            optionValues.put(QUESTIONS_ID_COL, questionId);

            // Insert the new option into the Options table.
            long optionId = db.insert(OPTIONS_TABLE_NAME, null, optionValues);

            // If this is the correct option, save its ID.
            if (i == correctOption) {
                correctOptionId = (int) optionId;
            }
        }

        // Update the Question table entry to include the ID of the correct option.
        ContentValues updateValues = new ContentValues();
        updateValues.put(ANSWER_COL, correctOptionId);
        db.update(QUESTION_TABLE_NAME, updateValues, QUESTIONS_ID_COL + " = ?", new String[]{String.valueOf(questionId)});
    }

    // Deletes a Question from the Database
    public Integer delete (int id) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Fetch the topic_id for this question
        Cursor cursor = db.query(QUESTION_TABLE_NAME, new String[]{TOPIC_ID_COL}, QUESTIONS_ID_COL + " = ?", new String[]{Integer.toString(id)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") int topic_id = cursor.getInt(cursor.getColumnIndex(TOPIC_ID_COL));

            // Delete the options associated with the question
            db.delete(OPTIONS_TABLE_NAME, QUESTIONS_ID_COL + " = ?", new String[]{Integer.toString(id)});

            // Delete the question
            db.delete(QUESTION_TABLE_NAME, QUESTIONS_ID_COL + " = ?", new String[]{Integer.toString(id)});

            // Check if the topic still exists and remove if it does not
            //This method also checks if the subject still exists and removes it if it does not
            removeTopic(topic_id);

            cursor.close();
        }
        return 0;
    }

    // Removes a Topic from the Database
    private void removeTopic(int topic_id) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Check if this topic_id exists in other questions
        Cursor cursor = db.query(QUESTION_TABLE_NAME, new String[]{TOPIC_ID_COL}, TOPIC_ID_COL + " = ?", new String[]{Integer.toString(topic_id)}, null, null, null);
        if (!(cursor != null && cursor.moveToFirst())) {
            // Fetch the subject_id for this topic
            Cursor cursor2 = db.query(TOPICS_TABLE_NAME, new String[]{SUBJECT_ID_COL}, TOPIC_ID_COL + " = ?", new String[]{Integer.toString(topic_id)}, null, null, null);
            if (cursor2 != null && cursor2.moveToFirst()) {
                @SuppressLint("Range") int subject_id = cursor2.getInt(cursor2.getColumnIndex(SUBJECT_ID_COL));

                // If this topic_id doesn't exist in other questions, delete the topic
                db.delete(TOPICS_TABLE_NAME, TOPIC_ID_COL + " = ?", new String[]{Integer.toString(topic_id)});

                // Check and remove subject
                removeSubject(subject_id);

                cursor2.close();
            }
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    // Removes a Subject from the Database
    private void removeSubject(int subject_id) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Check if this subject_id exists in other topics
        Cursor cursor = db.query(TOPICS_TABLE_NAME, new String[]{SUBJECT_ID_COL}, SUBJECT_ID_COL + " = ?", new String[]{Integer.toString(subject_id)}, null, null, null);
        if (!(cursor != null && cursor.moveToFirst())) {
            // If this subject_id doesn't exist in other topics, delete the subject
            db.delete(SUBJECTS_TABLE_NAME, SUBJECT_ID_COL + " = ?", new String[]{Integer.toString(subject_id)});
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    // Edits a Question in the Database
    @SuppressLint("Range")
    public boolean edit(Integer id, String name, String subject, String topic, String[] options, int correctOption) {
        // Get a writable instance of the SQLiteDatabase.
        SQLiteDatabase db = this.getWritableDatabase();

        // Fetch the old topic_id associated with this question ID.
        Cursor cursor = getData(id);
        int oldTopicID = -1;  // Initialize oldTopicID as -1, assuming it is not found
        if (cursor != null && cursor.moveToFirst()) {
            // Retrieve the old topic ID from the cursor if it exists.
            oldTopicID = cursor.getInt(cursor.getColumnIndex(TOPIC_ID_COL));
            cursor.close();  // Close the cursor to free resources
        }

        // Create a ContentValues object to hold updated values for the Question table.
        ContentValues contentValues = new ContentValues();
        // Update the question name
        contentValues.put(QUESTION_NAME_COL, name);

        // Check if the subject exists and retrieve its ID.
        int subjectID = checkSubject(subject);
        // Check if the topic exists under the subject and retrieve its ID.
        int topicID = checkTopic(subjectID, topic);

        // Update the Topic ID in the ContentValues object.
        contentValues.put(TOPIC_ID_COL, topicID);

        // Update the Question table with the new values where the question ID matches.
        db.update(QUESTION_TABLE_NAME, contentValues, QUESTIONS_ID_COL + " = ? ", new String[]{Integer.toString(id)});

        // Delete the existing options associated with this question ID.
        db.delete(OPTIONS_TABLE_NAME, QUESTIONS_ID_COL + " = ? ", new String[]{Integer.toString(id)});

        // Initialize variable to hold the ID of the correct option.
        int correctOptionId = -1;

        // Loop through the new options to insert them into the Options table.
        for (int i = 0; i < options.length; i++) {
            // Create ContentValues object to hold each option value.
            ContentValues optionValues = new ContentValues();
            // Insert option name and associate it with the question ID.
            optionValues.put(OPTION_NAME_COL, options[i]);
            optionValues.put(QUESTIONS_ID_COL, id);

            // Insert the option into the Options table.
            long optionId = db.insert(OPTIONS_TABLE_NAME, null, optionValues);

            // If this is the correct option, save its ID.
            if (i == correctOption) {
                correctOptionId = (int) optionId;
            }
        }

        // Create ContentValues object to hold the correct option ID for updating.
        ContentValues updateValues = new ContentValues();
        // Update with the correct option ID.
        updateValues.put(ANSWER_COL, correctOptionId);
        db.update(QUESTION_TABLE_NAME, updateValues, QUESTIONS_ID_COL + " = ?", new String[]{String.valueOf(id)});

        // Remove the old topic if it is no longer used.
        removeTopic(oldTopicID);

        return true;
    }

    // Updates the data when a Question is asked
    public void addAsked(int id, boolean isCorrect)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.query(QUESTION_TABLE_NAME, new String[]{NUM_TOTAL_COL, NUM_CORRECT_COL}, QUESTIONS_ID_COL + " = ?", new String[]{Integer.toString(id)}, null, null, null);
        if (cursor != null && cursor.moveToFirst())
        {
            @SuppressLint("Range") int total_num = cursor.getInt(cursor.getColumnIndex(NUM_TOTAL_COL));
            @SuppressLint("Range") int correct_num = cursor.getInt(cursor.getColumnIndex(NUM_CORRECT_COL));
            cursor.close();

            ContentValues contentValues = new ContentValues();
            contentValues.put(NUM_TOTAL_COL, total_num + 1);

            if(isCorrect)
            {
                contentValues.put(NUM_CORRECT_COL, correct_num + 1);
            }

            db.update(QUESTION_TABLE_NAME, contentValues, QUESTIONS_ID_COL + " = ?", new String[]{Integer.toString(id)});
        }
    }

    // Gets the data for a question
    public Cursor getData(int id)
    {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT " + QUESTION_TABLE_NAME + ".*, " +
                SUBJECTS_TABLE_NAME + "." + SUBJECT_NAME_COL + ", " +
                TOPICS_TABLE_NAME + "." + TOPIC_NAME_COL + ", " +
                QUESTION_TABLE_NAME + "." + ANSWER_COL + ", " + // Add ANSWER_COL here
                "GROUP_CONCAT(" + OPTIONS_TABLE_NAME + "." + OPTION_NAME_COL + ", ',') AS " + OPTION_NAME_COL + "," +
                "GROUP_CONCAT(" + OPTIONS_TABLE_NAME + "." + OPTIONS_ID_COL + ", ',') AS options_ids" + // Add OPTIONS_ID_COL as options_ids
                " FROM " + QUESTION_TABLE_NAME +
                " LEFT JOIN " + TOPICS_TABLE_NAME + " ON " + QUESTION_TABLE_NAME + "." + TOPIC_ID_COL + " = " + TOPICS_TABLE_NAME + "." + TOPIC_ID_COL +
                " LEFT JOIN " + SUBJECTS_TABLE_NAME + " ON " + TOPICS_TABLE_NAME + "." + SUBJECT_ID_COL + " = " + SUBJECTS_TABLE_NAME + "." + SUBJECT_ID_COL +
                " LEFT JOIN " + OPTIONS_TABLE_NAME + " ON " + QUESTION_TABLE_NAME + "." + QUESTIONS_ID_COL + " = " + OPTIONS_TABLE_NAME + "." + QUESTIONS_ID_COL +
                " WHERE " + QUESTION_TABLE_NAME + "." + QUESTIONS_ID_COL + " = ?" +
                " GROUP BY " + QUESTION_TABLE_NAME + "." + QUESTIONS_ID_COL;

        System.out.println(query);

        Cursor res = db.rawQuery(query, new String[]{String.valueOf(id)});

        return res;
    }

    // Executes an SQL Query
    public Cursor executeSQL (String SQL)
    {
        return this.getReadableDatabase().rawQuery(SQL, null);
    }

    // Returns all of the options for a Question
    @SuppressLint("Range")
    public HashMap<String, Integer> getOptions(int questionId) {
        HashMap<String, Integer> options = new HashMap<String, Integer>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + OPTION_NAME_COL + ", " + OPTIONS_ID_COL +
                " FROM " + OPTIONS_TABLE_NAME +
                " WHERE " + QUESTIONS_ID_COL + " = ?" +
                " ORDER BY " + OPTIONS_ID_COL + " ASC";  // order by the ID, to preserve insertion order
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(questionId)});
        if (cursor != null) {
            while (cursor.moveToNext()) {  // iterate through all rows
                options.put(cursor.getString(cursor.getColumnIndex(OPTION_NAME_COL)), cursor.getInt(cursor.getColumnIndex(OPTIONS_ID_COL)));
            }
            cursor.close();
        }
        return options;
    }

    // this method is called to check if the table exists already.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS " + QUESTION_TABLE_NAME);
        onCreate(db);
    }

    // Gets a List of Data entries containing a Search String in the Question, Topic, or Subject
    public Cursor search(String searchString)
    {
        //Get a readable database object from the current SQLiteOpenHelper object.
        SQLiteDatabase db = this.getReadableDatabase();

        //Build the SQL select query for searching.
        //The query uses INNER JOINs to link the QUESTION_TABLE, TOPICS_TABLE, and SUBJECTS_TABLE, and uses the alias "_id" for QUESTION_ID_COL for better compatibility with Android's CursorAdapters.
        //The query checks whether the search string is present in either the question, topic or subject
        String selectQuery = "SELECT *, " + QUESTION_TABLE_NAME + "." + QUESTIONS_ID_COL + " AS _id FROM " + QUESTION_TABLE_NAME
                + " INNER JOIN " + TOPICS_TABLE_NAME + " ON " + QUESTION_TABLE_NAME + "." + TOPIC_ID_COL + " = " + TOPICS_TABLE_NAME + "." + TOPIC_ID_COL
                + " INNER JOIN " + SUBJECTS_TABLE_NAME + " ON " + TOPICS_TABLE_NAME + "." + SUBJECT_ID_COL + " = " + SUBJECTS_TABLE_NAME + "." + SUBJECT_ID_COL
                + " WHERE " + QUESTION_TABLE_NAME + "." + QUESTION_NAME_COL + " LIKE ? OR "
                + SUBJECTS_TABLE_NAME + "." + SUBJECT_NAME_COL + " LIKE ? OR "
                + TOPICS_TABLE_NAME + "." + TOPIC_NAME_COL + " LIKE ?";

        //Perform the query using rawQuery.
        //The '?' in the query are replaced by the values in the String array in the same order.
        Cursor cursor = db.rawQuery(selectQuery, new String[] {"%" + searchString + "%", "%" + searchString + "%", "%" + searchString + "%"});

        //Return the Cursor containing the results of the query.
        //After returning the cursor, it is automatically applied to the adapter
        return cursor;
    }

    // Checks if a subject still has topics belonging to it and removes it if it doesn't
    private int checkSubject(String subject)
    {
        SQLiteDatabase db = this.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {SUBJECT_ID_COL};

        // Filter results WHERE "subject_name" = 'subject'
        String selection = SUBJECT_NAME_COL + " = ?";
        String[] selectionArgs = {subject};

        // How you want the results sorted in the resulting Cursor
        String sortOrder = SUBJECT_ID_COL + " DESC";

        Cursor cursor = db.query(
                SUBJECTS_TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );

        if (cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(SUBJECT_ID_COL));
            cursor.close();
            return id;
        } else {
            cursor.close();

            ContentValues contentValues = new ContentValues();
            contentValues.put(SUBJECT_NAME_COL, subject);

            long id = db.insert(SUBJECTS_TABLE_NAME, null, contentValues);
            return Math.toIntExact(id);
        }
    }

    // Checks if a subject still has questons belonging to it and removes it if it doesn't
    private int checkTopic(int subject, String topic)
    {
        SQLiteDatabase db = this.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {TOPIC_ID_COL};

        // Filter results WHERE "subject_id" = 'subject' AND "topic_name" = 'topic'
        String selection = SUBJECT_ID_COL + " = ? AND " + TOPIC_NAME_COL + " = ?";
        String[] selectionArgs = {String.valueOf(subject), topic};

        // How you want the results sorted in the resulting Cursor
        String sortOrder = TOPIC_ID_COL + " DESC";

        Cursor cursor = db.query(
                TOPICS_TABLE_NAME,      // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );

        if (cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(TOPIC_ID_COL));
            cursor.close();
            return id;
        } else {
            cursor.close();
            ContentValues contentValues = new ContentValues();
            contentValues.put(SUBJECT_ID_COL, subject);
            contentValues.put(TOPIC_NAME_COL, topic);

            long id = db.insert(TOPICS_TABLE_NAME, null, contentValues);
            return Math.toIntExact(id);
        }
    }

    // Gets a List of all Subjects
    @SuppressLint("Range")
    public HashMap<String, Integer> getSubjects()
    {
        SQLiteDatabase db = this.getReadableDatabase();

        HashMap<String, Integer> subjects = new HashMap<String, Integer>();
        String selectQuery = "SELECT " + SUBJECT_NAME_COL + ", " + SUBJECT_ID_COL + " FROM " + SUBJECTS_TABLE_NAME;

        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                subjects.put(cursor.getString(cursor.getColumnIndex(SUBJECT_NAME_COL)), cursor.getInt(cursor.getColumnIndex(SUBJECT_ID_COL)));
            }
            while (cursor.moveToNext());
        }

        cursor.close();

        return subjects;
    }

    // Gets a List of all the Topics for a Subject
    @SuppressLint("Range")
    public HashMap<String, Integer> getTopics(int subjectID)
    {
        SQLiteDatabase db = this.getReadableDatabase();

        HashMap<String, Integer> topics = new HashMap<String, Integer>();
        String selectQuery = "SELECT " + TOPIC_NAME_COL + ", " + TOPIC_ID_COL + " FROM " + TOPICS_TABLE_NAME + " WHERE " + SUBJECT_ID_COL + " = " + subjectID;

        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                topics.put(cursor.getString(cursor.getColumnIndex(TOPIC_NAME_COL)), cursor.getInt(cursor.getColumnIndex(TOPIC_ID_COL)));
            }
            while (cursor.moveToNext());
        }

        cursor.close();

        return topics;
    }
}
