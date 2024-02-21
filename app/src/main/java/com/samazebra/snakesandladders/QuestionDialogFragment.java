package com.samazebra.snakesandladders;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import java.util.HashMap;
import java.util.Random;

public class QuestionDialogFragment extends DialogFragment {

    // Member Variables
    private DBHandler dbHandler;
    public static boolean isAsking;
    private Board parent;
    private int randomId;
    private String sql;

    // Runs when layout loads
    @SuppressLint("Range")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.ask_question, null);

        dbHandler = new DBHandler(getContext());

        TextView questionText = view.findViewById(R.id.questionText);
        TextView subjectText = view.findViewById(R.id.subjectText);
        TextView topicText = view.findViewById(R.id.topicText);
        RadioGroup optionsGroup = view.findViewById(R.id.optionsGroup);
        TextView errorText = view.findViewById(R.id.errorText);

        Cursor cursor = selectQuestion();
        int correctAnswer = -1;
        String[] optionsIds = new String[0];
        if (cursor.moveToFirst()) {
            questionText.setText(cursor.getString(cursor.getColumnIndex(DBHandler.QUESTION_NAME_COL)));
            subjectText.setText(cursor.getString(cursor.getColumnIndex(DBHandler.SUBJECT_NAME_COL)));
            topicText.setText(cursor.getString(cursor.getColumnIndex(DBHandler.TOPIC_NAME_COL)));
            correctAnswer = cursor.getInt(cursor.getColumnIndex(DBHandler.ANSWER_COL));
            optionsIds = cursor.getString(cursor.getColumnIndex("options_ids")).split(",");

            // Assuming the options are comma-separated in the OPTION_NAME_COL
            String[] options = cursor.getString(cursor.getColumnIndex(DBHandler.OPTION_NAME_COL)).split(",");
            for (String option : options) {
                RadioButton radioButton = new RadioButton(getContext());
                radioButton.setText(option);
                optionsGroup.addView(radioButton);
            }
        }
        cursor.close();

        Button submitBtn = view.findViewById(R.id.submitBtn);
        String[] finalOptionsIds = optionsIds;
        int finalCorrectAnswer = correctAnswer;
        submitBtn.setOnClickListener(v -> {
            // Get the ID of the selected option
            int selectedId = optionsGroup.getCheckedRadioButtonId();
            // Check if an option is selected
            if (selectedId == -1) {
                // Show an error message in red if no option is selected
                errorText.setTextColor(Color.RED);
                errorText.setVisibility(View.VISIBLE);
                errorText.setText("Please select an answer.");
            } else {
                // Check if the ID of the selected answer matches that of the correct answer
                RadioButton selectedOption = view.findViewById(selectedId);
                int selectedIndex = optionsGroup.indexOfChild(selectedOption);
                if (Integer.parseInt(finalOptionsIds[selectedIndex]) == finalCorrectAnswer)
                {
                    // Display "Correct Answer" in green if the answer is correct
                    errorText.setTextColor(Color.GREEN);
                    errorText.setText("Correct Answer");
                    // Update the database
                    dbHandler.addAsked(randomId, true);
                } else {
                    // Display "Incorrect Answer" in red if the answer is correct
                    errorText.setTextColor(Color.RED);
                    errorText.setText("Incorrect Answer");
                    // Update the database
                    dbHandler.addAsked(randomId, false);
                }
                // Show the  text
                errorText.setVisibility(View.VISIBLE);

                // Move the bot if its turn is next
                isAsking = false;
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dismiss();
                        if (Board.isBot && Board.isTurn2 && Board.isRollDice)
                        {
                            parent.moveBot(parent.player1, parent.player2, parent.dice1);
                        }
                    }
                }, 1000);
            }
        });

        isAsking = true;

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                //.setTitle("Answer the following question")
                .setView(view)
                .setCancelable(false)
                .create();

        dialog.setCanceledOnTouchOutside(false);

        dialog.setOnKeyListener((dialog1, keyCode, event) -> {
            if (keyCode == android.view.KeyEvent.KEYCODE_BACK) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                Fragment fragment = new Start();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragmentHolder, fragment);
                fragmentTransaction.addToBackStack(fragment.toString());
                fragmentTransaction.commit();
                return false;
            }
            return false;
        });

        return dialog;
    }

    // Sets Parent Data
    public void setDetails(Board parent)
    {
        this.parent = parent;
    }

    // Selects a Question to Ask
    private Cursor selectQuestion() {
        // Initialize a Cursor to execute the SQL query and retrieve the data.
        Cursor data = dbHandler.executeSQL(sql);

        // Create a HashMap to store the accuracy rate for each question ID.
        HashMap<Integer, Double> accuracy = new HashMap<Integer, Double>();
        // Create a HashMap to store the mapping of number ranges to question IDs.
        HashMap<Long, Integer> IDNumbers = new HashMap<Long, Integer>();

        if (data.moveToFirst()) {
            // Loop through the Cursor rows.
            do {
                // Get the question ID from the current row.
                @SuppressLint("Range") int id = data.getInt(data.getColumnIndex(DBHandler.QUESTIONS_ID_COL));
                // Calculate the accuracy rate for the current question.
                @SuppressLint("Range") double accuracyRate = (double) data.getInt(data.getColumnIndex(DBHandler.NUM_CORRECT_COL)) / (double) data.getInt(data.getColumnIndex(DBHandler.NUM_TOTAL_COL));
                // Store the accuracy rate in the HashMap with the question ID as the key.
                accuracy.put(id, accuracyRate);
            } // Continue looping until there are no more rows in the Cursor.
            while (data.moveToNext());
        }
        // Close the Cursor to free up resources.
        data.close();

        // Initialize a variable to keep track of the start number for the range mapping.
        long startNum = 0;

        // Iterate over each entry in the accuracy HashMap.
        for (int id : accuracy.keySet()) {
            // Calculate the inaccuracy rate by subtracting the accuracy rate from 1.
            double incorrectRateDouble = 1 - accuracy.get(id);
            // Convert the inaccuracy rate to a long after scaling it up by 1000 and incrementing by 1.
            long incorrectRate = Math.round(++incorrectRateDouble * 1000);

            // Create a range of numbers corresponding to the inaccuracy rate and map it to the question ID.
            for (long num = startNum; num < startNum + incorrectRate; num++) {
                IDNumbers.put(num, id);
            }

            // Update the start number for the next range of numbers.
            startNum += incorrectRate;
        }

        // Create an instance of Random to generate a random number.
        Random random = new Random();
        // Select a random number from the range of keys in the IDNumbers HashMap.
        randomId = IDNumbers.get((long)random.nextInt(IDNumbers.keySet().size()));

        // Retrieve the Cursor for the randomly selected question ID and return it.
        return dbHandler.getData(randomId);
    }

    // Constructor
    public QuestionDialogFragment(String sql)
    {
        this.sql = sql;
    }
}
