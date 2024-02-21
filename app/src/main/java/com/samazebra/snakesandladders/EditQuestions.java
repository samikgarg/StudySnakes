package com.samazebra.snakesandladders;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EditQuestions extends DialogFragment {

    // All Member Variables
    QuestionsSearch parent;
    private LinearLayout optionsLayout;
    private int optionCount = 0;
    private ArrayList<String> options;
    private ArrayAdapter<String> optionsAdapter;
    private Spinner correctOptionSpinner;
    private Button generateQuestion;
    private TextView errorTV;
    private ArrayList<EditText> optionInputs;
    EditText question;
    Cursor data;
    boolean isEdit = false;
    int id = -1;
    AutoCompleteTextView subjectInput;
    AutoCompleteTextView topicInput;
    HashMap<String, Integer> subjects;
    DBHandler handler;

    // Function will run as soon as layout loads
    @SuppressLint({"MissingInflatedId", "Range"})
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.edit_questions, null);

        handler = new DBHandler(getActivity());

        optionsLayout = view.findViewById(R.id.options_layout);
        correctOptionSpinner = view.findViewById(R.id.correct_option_spinner);
        options = new ArrayList<>();
        optionsAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, options);
        optionsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        correctOptionSpinner.setAdapter(optionsAdapter);

        question = view.findViewById(R.id.question_text);
        subjectInput = view.findViewById(R.id.subject_input);
        topicInput = view.findViewById(R.id.topic_input);

        generateQuestion = view.findViewById(R.id.generate_question);

        errorTV = view.findViewById(R.id.error);
        errorTV.setVisibility(View.INVISIBLE);

        subjects = handler.getSubjects();
        List<String> subjectslist = new ArrayList<>(subjects.keySet());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, subjectslist);
        subjectInput.setAdapter(adapter);
        optionInputs = new ArrayList<>();

        // Adds the Question Data to the form if a Question is being edited
        if (isEdit)
        {
            if (data != null && data.moveToFirst())
            {
                @SuppressLint("Range") String questionName = data.getString(data.getColumnIndex(DBHandler.QUESTION_NAME_COL));
                @SuppressLint("Range") String subjectName = data.getString(data.getColumnIndex(DBHandler.SUBJECT_NAME_COL));
                @SuppressLint("Range") String topicName = data.getString(data.getColumnIndex(DBHandler.TOPIC_NAME_COL));
                HashMap<String, Integer> optionsList = handler.getOptions(id);
                @SuppressLint("Range") int correctAnswerID = data.getInt(data.getColumnIndex(DBHandler.ANSWER_COL));

                int position = 0;
                for (String option : optionsList.keySet())
                {
                    EditText editText = addOptionField();
                    editText.setText(option);

                    if (optionsList.get(option) == correctAnswerID)
                    {
                        correctOptionSpinner.setSelection(position);
                    }

                    position ++;
                }

                question.setText(data.getString(data.getColumnIndex(DBHandler.QUESTION_NAME_COL)));
                subjectInput.setText(subjectName);
                topicInput.setText(topicName);
            }
        }
        else
        {
            addOptionField();
            addOptionField();
        }

        // Sets the list of topics for the Subject inputted so that they are auto-filled when the user is inputting the Topic for the Question
        subjectInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                setTopics();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        // Sets operation of Generate Question Button
        generateQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GenerateQuestion generateQuestion = new GenerateQuestion();
                generateQuestion.setParent(EditQuestions.this);
                generateQuestion.show(getChildFragmentManager(), "generate_question_tag");
            }
        });

        // Sets operation of Add Option Button
        Button addOption = view.findViewById(R.id.add_option);
        addOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addOptionField();
            }
        });

        // Sets the Positive, Negative and Neutral Buttons part of the Dialog Box
        if (isEdit)
        {
            builder.setView(view)
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            save();
                            parent.updateList(parent.searchString);
                        }
                    })
                    .setNeutralButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            handler.delete(id);
                            parent.updateList(parent.searchString);
                        }
                    });
        }
        else
        {
            builder.setView(view)
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {

                        }
                    });
        }

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        dialog.setOnShowListener(new DialogInterface.OnShowListener()
                                 {
                                     @Override
                                     public void onShow(DialogInterface dialogInterface) {
                                         Button buttonPositive = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                                         Button buttonNegative = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                                         Button buttonNeutral = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEUTRAL);

                                         buttonPositive.setTextColor(Color.WHITE);
                                         buttonNegative.setTextColor(Color.WHITE);
                                         buttonNeutral.setTextColor(Color.WHITE);

                                         ViewGroup parent2 = (ViewGroup) buttonPositive.getParent();
                                         parent2.setBackgroundResource(R.drawable.button_border);

                                         buttonPositive.setOnClickListener(new View.OnClickListener() {
                                             @Override
                                             public void onClick(View view) {
                                                 //If there are no issues with the inputted data
                                                 if (isNoError())
                                                 {
                                                     //Save the data to the database
                                                     save();
                                                     //Update the list in the Question Search Page so that it shows the updated information
                                                     parent.updateList(parent.searchString);
                                                     //Dismisses the dialog box
                                                     dialog.dismiss();
                                                 }
                                             }
                                         });
                                     }
                                 });

        setTopics();

        return dialog;
    }

    // Sets the list of Topics so that they are auto-filled when the user inputs a Topic
    private void setTopics()
    {
        HashMap<String, Integer> topics;

        if (subjects.get(subjectInput.getText().toString().trim()) != null)
        {
            topics = handler.getTopics(subjects.get(subjectInput.getText().toString().trim()));
        }
        else
        {
            topics = handler.getTopics(-1);
        }

        List<String> topicsList = new ArrayList<>(topics.keySet());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, topicsList);
        topicInput.setAdapter(adapter);
    }

    // Adds an Option Field to the Screen
    private EditText addOptionField() {
        optionCount++;

        LinearLayout fullOptionLayout = new LinearLayout(getContext());
        fullOptionLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParamsFull = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParamsFull.setMargins(10, 10, 10, 10);
        fullOptionLayout.setLayoutParams(layoutParamsFull);

        LinearLayout newOptionLayout = new LinearLayout(getContext());
        newOptionLayout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(10, 10, 10, 10);
        newOptionLayout.setLayoutParams(layoutParams);


        Button btnDeleteOption = new Button(getContext());
        btnDeleteOption.setText("-");
        btnDeleteOption.setId(View.generateViewId());

        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(100, 100);
        btnDeleteOption.setLayoutParams(buttonParams);

        // Create a circular shape for the button
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.OVAL);
        shape.setSize(10,10);
        shape.setColor(Color.RED); //change to the color you want
        btnDeleteOption.setBackground(shape);

        // Center the button in the LinearLayout
        newOptionLayout.setGravity(Gravity.CENTER_VERTICAL);

        btnDeleteOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeOptionField(fullOptionLayout);

                for (int i = 0; i < optionsLayout.getChildCount(); i++) {
                    LinearLayout fullOptionLayout = (LinearLayout) optionsLayout.getChildAt(i);
                    TextView optionLabel = (TextView) fullOptionLayout.getChildAt(0);
                    optionLabel.setText("Option " + (i + 1));
                }
            }
        });

        // Create new option EditText
        EditText newOptionEditText = new EditText(getContext());
        newOptionEditText.setHint("                                            ");
        newOptionEditText.setId(View.generateViewId());
        newOptionEditText.setTextColor(Color.WHITE);

        newOptionEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int index = optionsLayout.indexOfChild(fullOptionLayout);
                if (index >= 0 && index < options.size()) {
                    options.set(index, s.toString());
                    optionsAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }

        });

        newOptionLayout.addView(btnDeleteOption);
        newOptionLayout.addView(newOptionEditText);

        TextView label = new TextView(getContext());
        label.setText("Option " + optionCount);
        label.setId(View.generateViewId());
        label.setTextColor(Color.WHITE);

        fullOptionLayout.addView(label);
        fullOptionLayout.addView(newOptionLayout);

        optionsLayout.addView(fullOptionLayout);
        options.add("");  // Add a new empty option
        optionsAdapter.notifyDataSetChanged();  // Notify the adapter that the data set has changed

        optionInputs.add(newOptionEditText);

        return newOptionEditText;
    }

    // Checks if there is an error in the inputs and displays an error message
    @SuppressLint("Range")
    private boolean isNoError() {
        // Initialize counter for the number of errors
        int noErrors = 0;

        // Initialize the error message string
        String errorString = "Please fill in ";

        // Initialize a flag for checking if topic and subject are the same in the database
        boolean isSameTopic = false;

        // ArrayList to hold the error messages
        ArrayList<String> errors = new ArrayList<String>();

        // Check if the question field is empty
        if (question.getText().length() == 0) {
            errors.add("the Question");
            noErrors++;
        }

        // Check if the subject field is empty
        if (subjectInput.getText() == null || subjectInput.getText().toString().length() == 0) {
            errors.add("the Subject");
            noErrors++;
        }

        // Check if the topic field is empty
        if (topicInput.getText() == null || topicInput.getText().toString().length() == 0) {
            errors.add("the Topic");
            noErrors++;
        }
        // Check if the topic already exists in the database and if so, whether the associated subject matches
        else if (subjectInput.getText() != null || subjectInput.getText().toString().length() != 0) {
            String topic = topicInput.getText().toString();
            String subject = subjectInput.getText().toString();

            // Query database to find the count of this topic
            int noTopics = 0;
            Cursor cursor = handler.executeSQL("SELECT COUNT(" + DBHandler.TOPIC_ID_COL + ") FROM " + DBHandler.TOPICS_TABLE_NAME + " WHERE " + DBHandler.TOPIC_NAME_COL + " = \"" + topic + "\"");
            if (cursor.moveToFirst()) {
                noTopics = cursor.getInt(0);
            }

            // If the topic already exists, check if the associated subject matches
            if (noTopics > 0) {
                String subjectInTopic = "";
                Cursor subjectCursor = handler.executeSQL("SELECT " + DBHandler.SUBJECT_NAME_COL + " FROM " + DBHandler.TOPICS_TABLE_NAME + " JOIN " + DBHandler.SUBJECTS_TABLE_NAME + " ON (" + DBHandler.TOPICS_TABLE_NAME + "." + DBHandler.SUBJECT_ID_COL + "=" + DBHandler.SUBJECTS_TABLE_NAME + "." + DBHandler.SUBJECT_ID_COL + ") WHERE " + DBHandler.TOPIC_NAME_COL + " = \"" + topic + "\"");
                if (subjectCursor.moveToFirst()) {
                    subjectInTopic = subjectCursor.getString(subjectCursor.getColumnIndex(DBHandler.SUBJECT_NAME_COL));
                }

                if (!subjectInTopic.equals(subject)) {
                    isSameTopic = true;
                }
            }
        }

        // Check if all options fields are filled
        for (EditText et : optionInputs) {
            if (et.getText().length() == 0) {
                errors.add("all of the Options");
                noErrors++;
                break;
            }
        }

        // Construct the final error string based on the ArrayList of errors
        for (int i = 0; i < errors.size(); i++) {
            if (i == errors.size() - 2) {
                errorString += errors.get(i) + ", and ";
            } else if (i == errors.size() - 1) {
                errorString += errors.get(i);
            } else {
                errorString += errors.get(i) + ", ";
            }
        }

        // Terminate the error string with a period
        errorString += ".";

        // If topic and subject don't match, update errorString
        if (isSameTopic) {
            if (noErrors == 0) {
                errorString = "Please use a Unique Topic";
            } else {
                errorString += " Please also use a Unique Topic";
            }
            noErrors++;
        }

        // Check if there are at least two options and update errorString if not
        if (options.size() < 2) {
            if (noErrors == 0) {
                errorString = "Please add at least two options.";
            } else {
                errorString += " Please also add at least two options.";
            }
            noErrors++;
        }

        // Display the error message
        errorTV.setText(errorString);

        // Toggle visibility of the error message based on if there are errors
        if (noErrors == 0) {
            errorTV.setVisibility(View.INVISIBLE);
            return true;
        } else {
            errorTV.setVisibility(View.VISIBLE);
            return false;
        }
    }

    // Removes an Option Field
    private void removeOptionField(LinearLayout fullOptionLayout) {
        int index = optionsLayout.indexOfChild(fullOptionLayout);
        if (index >= 0 && index < options.size()) {
            options.remove(index);
            optionInputs.remove(index);
            optionsLayout.removeView(fullOptionLayout);
            optionCount--;
            optionsAdapter.notifyDataSetChanged();
        }
    }

    // Sets the Cursor for the Question being edited
    public void setData(Cursor data, int id)
    {
        this.data = data;
        isEdit = true;
        this.id = id;
    }

    // Removes all Options enterred
    private void deleteAllOptions() {
        options.clear(); // Clears the ArrayList
        optionInputs.clear();
        optionsLayout.removeAllViews(); // Removes all views from the LinearLayout
        optionCount = 0; // Reset option count
        optionsAdapter.notifyDataSetChanged(); // Notify the adapter that the data set has changed
    }

    // Sets a Question generated using the OpenAI API
    public void setGeneration(final String response)
    {
        System.out.println(response);
        // Split the response string by semicolons and take the first part as the question
        // Trim to remove any leading or trailing white spaces
        final String questionString = response.split(";")[0].trim();

        // Split the second part of the response string by commas to extract the options
        // Trim each option to remove any leading or trailing white spaces
        final String[] options = response.split(";")[1].trim().split(",");

        // Initialize a variable to hold the index of the correct answer within the options array
        int correctIndex = 0;

        try {
            // Attempt to parse the third part of the response string to get the index of the correct answer
            // This assumes that the correct index is represented as an integer in the response string
            correctIndex = Integer.parseInt(response.split(";")[2].trim());
        } catch (Exception e) {
            // In case parsing the integer fails, look for the correct answer string among the options
            // This loop goes through each option to check if it contains the string of the correct answer
            for (int i = 0; i < options.length; i++) {
                if (options[i].contains(response.split(";")[2].trim())) {
                    // If found, set the correctIndex to the index of this option
                    correctIndex = i;
                    // Exit the loop as the correct index has been found
                    break;
                }
            }
        }

        // Creates  final index variable for setting the selection on the Spinner
        final int finalCorrectIndex = correctIndex;

        // Execute code on the main UI thread to update the interface elements
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Add the question to the UI
                question.setText(questionString);

                // Delete all existing options from the UI
                deleteAllOptions();

                //Add all of the generated options to the UI
                for (final String option : options)
                {
                    final EditText editText = addOptionField();
                    editText.setText(option.trim());
                }

                //Set the correct option in the spinner
                correctOptionSpinner.setSelection(finalCorrectIndex - 1);
            }
        });
    }

    // Sets the parent layout
    public void setParent (QuestionsSearch parent)
    {
        this.parent = parent;
    }

    // Saves the Data
    private void save()
    {
        String[] optionsAdded = new String[options.size()];

        for (int i = 0; i < optionsAdded.length; i++)
        {
            optionsAdded[i] = options.get(i);
        }

        //Check if a question is being edited or if a new one is being added
        if (isEdit) {
            //Update the existing entry with the new informaion
            handler.edit(id, question.getText().toString(), subjectInput.getText().toString(), topicInput.getText().toString(), optionsAdded, correctOptionSpinner.getSelectedItemPosition());
        }
        else {
            //Add the new question to the database
            handler.addNew(question.getText().toString(), subjectInput.getText().toString(), topicInput.getText().toString(), optionsAdded, correctOptionSpinner.getSelectedItemPosition());
        }
    }
}
