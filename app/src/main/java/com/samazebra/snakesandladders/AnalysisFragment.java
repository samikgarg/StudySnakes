package com.samazebra.snakesandladders;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.text.TextWatcher;
import android.text.Editable;

import java.util.ArrayList;

public class AnalysisFragment extends Fragment {

    // All Member Variables
    private TextView averageScoreTextView;
    private TextView futureScoreTextView;
    private EditText goalEditText;
    TextView tvGoal;

    private ArrayList<Integer> timesAsked;
    private ArrayList<Integer> numberCorrect;

    // Method runs right after layout loads
    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.analysis, container, false);

        averageScoreTextView = rootView.findViewById(R.id.average_score);
        futureScoreTextView = rootView.findViewById(R.id.future_score);
        goalEditText = rootView.findViewById(R.id.goal);
        tvGoal = rootView.findViewById(R.id.goal_tv);

        goalEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Empty
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Empty
            }

            @Override
            public void afterTextChanged(Editable editable) {
                analyse();
            }
        });

        setData();

        return rootView;
    }

    // Constructor to set Member Variables.
    public AnalysisFragment(ArrayList<Integer> timesAsked, ArrayList<Integer> numberCorrect)
    {
        this.timesAsked = timesAsked;
        this.numberCorrect = numberCorrect;
    }

    // Logs the data for testing and sets the Average Score
    public void setData ()
    {
        for (int a : timesAsked)
        {
            Log.i("Times Asked", "" + a);
        }
        for (int c : numberCorrect)
        {
            Log.i("Number Correct", "" + c);
        }

        setAverage();
    }

    // Calculate and set the Average Score
    public void setAverage()
    {
        //Gets the total number of questions asked
        int totalAsked = sumArrayList(timesAsked);
        //Gets the total number of questions answered correctly
        int totalCorrect = sumArrayList(numberCorrect);

        //If there are asked questions in the question set
        if (totalAsked != 0) {
            //Calculate the accuracy as a percentage to two decimal places
            double accuracy = (double)Math.round(((double)totalCorrect/(double)totalAsked) * 10000.0)/100.0;
            //Show the accuracy rate
            averageScoreTextView.setText("Average Score\n" + accuracy + "%");
            //Make the future test score invisible until the user enters a goal
            futureScoreTextView.setVisibility(View.INVISIBLE);
        }
        //If there are no asked errors in the question set
        else {
            //Show an error message
            averageScoreTextView.setText("There are no asked questions");
            //Remove all other UI elements
            goalEditText.setVisibility(View.GONE);
            tvGoal.setVisibility(View.GONE);
            futureScoreTextView.setVisibility(View.GONE);
        }
    }

    // Calculates and displays the future number of questions using the goal
    private void analyse() {
        try {
            // Parse the user-inputted goal from the EditText field.
            double goal = Double.parseDouble(goalEditText.getText().toString());

            // Calculate the sum of correct answers and total questions using the sumArrayList method.
            double correct = sumArrayList(numberCorrect);
            double total = sumArrayList(timesAsked);

            // Compute the current average score with a precision of two decimal places.
            double averageHere = (double)Math.round(((double)correct/(double)total) * 10000.0)/100.0;

            // Check all possible goal scenarios to provide actionable advice to the user.

            // Scenario 1: Goal is 0. In this case, the user will achieve it no matter what.
            if (goal == 0) {
                futureScoreTextView.setText("Your goal will be achieved no matter the number of questions you get wrong.");
            }
            // Scenario 2: Goal is 100. Either they achieve it or they don't based on the current average.
            else if (goal == 100) {
                if (averageHere == 100) {
                    futureScoreTextView.setText("Congratulations, You have achieved your goal! However, you cannot get anymore questions wrong.");
                } else {
                    futureScoreTextView.setText("You cannot achieve your goal no matter the number of questions you get correct.");
                }
            }
            // Scenario 3: Goal is between 0 and 100. Calculate exact number of questions needed to meet the goal.
            else if (goal > averageHere) {
                // Using Math.ceil() for worst-case scenario, calculate the number of questions to get right.
                int number = (int) Math.ceil((correct - (goal/100)*total)/((goal/100) - 1));

                // Handling edge case when the number turns out to be zero.
                if (number == 0) {
                    number++;
                }

                futureScoreTextView.setText("You need to get " + number + " more questions correct to achieve your goal.");
            }
            // Scenario 4: User has already achieved or surpassed their goal.
            else if (goal < averageHere) {
                // Using Math.floor() for best-case scenario, calculate the number of questions they can afford to get wrong.
                int number = (int) Math.floor((correct - (goal/100)*total)/((goal/100)));

                // Handling edge case when the number turns out to be zero.
                if (number == 0) {
                    futureScoreTextView.setText("Congratulations, You have achieved your goal! However, you cannot get anymore questions wrong.");
                } else {
                    futureScoreTextView.setText("Congratulations, You have achieved your goal! You can get " + number + " more questions wrong while staying above your goal.");
                }
            }
            // Scenario 5: User has exactly achieved their goal.
            else {
                futureScoreTextView.setText("Congratulations, You have achieved your goal! However, you cannot get anymore questions wrong.");
            }

            // Make the futureScoreTextView visible to show the analysis result.
            futureScoreTextView.setVisibility(View.VISIBLE);
        }
        catch (Exception e) {
            // Handle invalid input or empty field.
            if (goalEditText.getText().toString().trim().equals("")) {
                futureScoreTextView.setVisibility(View.INVISIBLE);
            } else {
                futureScoreTextView.setText("Please enter a valid goal");
                futureScoreTextView.setVisibility(View.VISIBLE);
            }
        }
    }

    // Sums all values in an ArrayList
    private int sumArrayList(ArrayList<Integer> al)
    {
        int sum = 0;
        for (int i : al)
        {
            sum += i;
        }
        return sum;
    }
}

