package com.samazebra.snakesandladders;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

public class Filter extends Fragment {

    // All Member Variables
    private LinearLayout optionsContainer;
    private Spinner filterSpinner;
    private TextView errorTV;
    private StringBuilder sqlSB;
    private int noFilters;
    private ArrayList<FilterUI> filters = new ArrayList<>();
    private boolean isAnalyse;
    private boolean isBot;
    DBHandler dbHandler;

    // Function runs automatically when layout is loaded
    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.filter, container, false);
        optionsContainer = view.findViewById(R.id.optionsContainer);
        filterSpinner = view.findViewById(R.id.filterSpinner);
        Button addButton = view.findViewById(R.id.addButton);
        Button submitButton = view.findViewById(R.id.submitButton);
        errorTV = view.findViewById(R.id.errorTV);
        errorTV.setVisibility(View.INVISIBLE);

        String[] filters = {"Question", "Subject and Topic", "Times Asked"};
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, filters);
        filterSpinner.setAdapter(filterAdapter);

        addButton.setOnClickListener(v -> {
            String filterBy = filterSpinner.getSelectedItem().toString();
            addFilterOption(filterBy);
        });

        submitButton.setOnClickListener(v ->
        {
            submit();
        });

        noFilters = 0;

        dbHandler = new DBHandler(getContext());

        addSubjectAndTopicOption();
        noFilters++;

        return view;
    }

    // Submits Filter
    @SuppressLint("Range")
    private void submit() {
        // Check if there are any errors using the isNoError() method.
        if (isNoError()) {
            String sql = "";

            // Initialize StringBuilder to construct the SQL query.
            // The SQL query performs multiple LEFT JOIN operations linking questions, topics, subjects, and options tables.
            sqlSB = new StringBuilder("SELECT * FROM " + DBHandler.QUESTION_TABLE_NAME +
                    " LEFT JOIN " + DBHandler.TOPICS_TABLE_NAME + " ON " +
                    DBHandler.QUESTION_TABLE_NAME + "." + DBHandler.TOPIC_ID_COL + " = " +
                    DBHandler.TOPICS_TABLE_NAME + "." + DBHandler.TOPIC_ID_COL +
                    " LEFT JOIN " + DBHandler.SUBJECTS_TABLE_NAME + " ON " +
                    DBHandler.TOPICS_TABLE_NAME + "." + DBHandler.SUBJECT_ID_COL + " = " +
                    DBHandler.SUBJECTS_TABLE_NAME + "." + DBHandler.SUBJECT_ID_COL +
                    " LEFT JOIN " + DBHandler.OPTIONS_TABLE_NAME + " ON " +
                    DBHandler.QUESTION_TABLE_NAME + "." + DBHandler.QUESTIONS_ID_COL + " = " +
                    DBHandler.OPTIONS_TABLE_NAME + "." + DBHandler.QUESTIONS_ID_COL + " ");

            // Check if any filters should be applied, stored in the filters ArrayList.
            if (noFilters > 0) {
                // Add the WHERE clause to the SQL query to accommodate filtering.
                sqlSB.append("WHERE ");

                // Loop through each filter in the filters ArrayList.
                for (int i = 0; i < filters.size(); i++) {
                    FilterUI currFilter = filters.get(i);
                    int type = currFilter.type;

                    // Depending on the type of filter, dynamically add conditions to the WHERE clause.
                    switch (type) {
                        case 0:  // AndOrFilter
                            //Cast the current filter to a AndOrFilter
                            AndOrFilter aof = (AndOrFilter) currFilter;
                            //Append "AND" or "OR" to connect the conditions
                            sqlSB.append(aof.spinner.getSelectedItem().toString().toUpperCase()).append(" ");
                            break;

                        case 1:  // QuestionFilter
                            //Cast the current filter to a QuestionFilter
                            QuestionFilter qf = (QuestionFilter) currFilter;
                            //Get the User Inputs
                            int selected = qf.spinner.getSelectedItemPosition();
                            String item = qf.editText.getText().toString().trim();
                            //Append the Question Column Name and a Space to add the condition
                            sqlSB.append(DBHandler.QUESTION_NAME_COL + " ");
                            // Implement different SQL conditions based on user's choice.
                            switch (selected) {
                                case 0: //"Is"
                                    sqlSB.append("= \"").append(item).append("\" ");
                                    break;
                                case 1: //"Contains"
                                    sqlSB.append("LIKE \"%").append(item).append("%\" ");
                                    break;
                                case 2: //"Begins With"
                                    sqlSB.append("LIKE \"%").append(item).append("\" ");
                                    break;
                                case 3: //"Ends With"
                                    sqlSB.append("LIKE \"").append(item).append("%\" ");
                                    break;
                            }
                            break;

                        case 2:  // SubjectTopicFilter
                            //Cast the current filter to a SubjectTopicFilter
                            SubjectTopicFilter stf = (SubjectTopicFilter) currFilter;
                            //Get the User Inputs
                            String subject = stf.spinnerSubject.getSelectedItem().toString();
                            String topic = stf.spinnerTopic.getSelectedItem().toString();
                            // Implement different SQL conditions based on subject and topic.
                            //If a subject is specified but a topic is not
                            if (topic.equals("Any") && !subject.equals("Any")) {
                                sqlSB.append(DBHandler.SUBJECT_NAME_COL + " = \"").append(subject).append("\" ");
                            }
                            //If a topic is specified
                            else if (!topic.equals("Any")) {
                                sqlSB.append(DBHandler.TOPIC_NAME_COL + " = \"").append(topic).append("\" ");
                            }
                            //If no topic or subject is specified
                            else {
                                sqlSB.append(DBHandler.TOPIC_NAME_COL + " LIKE \"%%\"");
                            }
                            break;

                        case 3:  // TimesAskedFilter
                            //Cast the current filter to a SubjectTopicFilter
                            TimesAskedFilter taf = (TimesAskedFilter) currFilter;
                            //Get the User Inputs
                            int inequality = taf.spinner.getSelectedItemPosition();
                            int value = Integer.parseInt(taf.editText.getText().toString());
                            //Append the Question Column Name and a Space to add the condition
                            sqlSB.append(DBHandler.NUM_TOTAL_COL + " ");
                            // Implement different SQL conditions based on inequality.
                            switch (inequality) {
                                case 0: //"Equal To"
                                    sqlSB.append("= ");
                                    break;
                                case 1: //"Greater Than"
                                    sqlSB.append("> ");
                                    break;
                                case 2: //"Lesser Than"
                                    sqlSB.append("< ");
                                    break;
                                case 3: //"Greater or Equal To"
                                    sqlSB.append(">= ");
                                    break;
                                case 4: //"Lesser or Equal To"
                                    sqlSB.append("<= ");
                                    break;
                            }
                            //Append the inputted value
                            sqlSB.append(value).append(" ");
                            break;
                    }
                }
            }

            // Finalize the SQL query.
            sql = sqlSB.toString().trim();

            // Log the SQL query for debugging.
            Log.i("SQL", "" + sql);

            // Execute the SQL query and retrieve results.
            Cursor cursor = dbHandler.executeSQL(sql);

            // Initialize ArrayLists for storing unique question IDs, times asked, and times answered correctly.
            ArrayList<Integer> IDs = new ArrayList<>();
            ArrayList<Integer> timesAskedAL = new ArrayList<>();
            ArrayList<Integer> numberCorrectAL = new ArrayList<>();

            // Iterate through the result set to fetch data.
            if (cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(DBHandler.QUESTIONS_ID_COL));

                    // Store only unique IDs along with times each question was asked and answered correctly.
                    if (!IDs.contains(id)) {
                        timesAskedAL.add(cursor.getInt(cursor.getColumnIndex(DBHandler.NUM_TOTAL_COL)));
                        numberCorrectAL.add(cursor.getInt(cursor.getColumnIndex(DBHandler.NUM_CORRECT_COL)));
                        IDs.add(id);
                    }
                } while (cursor.moveToNext());
            }

            // Close the cursor to release resources.
            cursor.close();

            // Based on the value of the isAnalyse flag, either open the analysis screen or proceed to the game screen.
            if (isAnalyse) {
                openAnalysis(timesAskedAL, numberCorrectAL);
            } else {
                openGame(sql);
            }
        }
    }

    // Checks for Errors in Data Entry
    private boolean isNoError() {
        //boolean to store the final result of the method
        boolean isNoError = true;
        //booleans to keep track of the errors that have occurred
        boolean isTextError = false;
        boolean isNumberError = false;

        //Loops through all of the filters to check for errors
        for (int i = 0; i < filters.size(); i++) {
            //Gets the FilterUI Object
            FilterUI currFilter = filters.get(i);

            //If it is a Question Filter
            if (currFilter.type == 1) {
                //Cast to a Question Filter Object
                QuestionFilter qf = (QuestionFilter) currFilter;

                //If nothing has been entered into the text box
                if (qf.editText.getText().toString().trim().equals("")) {
                    //Sets final return value to false since there is now an error
                    isNoError = false;

                    //Checks whether a number error has occurred
                    if (isNumberError) {
                        //Concatenate the Strings to show both a Number and Empty String Error
                        errorTV.setText("Please Enter a Valid Number for Times Asked and Please Fill All Text Fields");
                    }
                    else {
                        //Shows only the Text Error
                        errorTV.setText("Please Fill All Text Fields");
                    }

                    //Sets Error TextView to Visible
                    errorTV.setVisibility(View.VISIBLE);
                    //There has now been an empty text error, so this value is set to true
                    isTextError = true;
                }
            }

            //If it is a Question Filter
            if (currFilter.type == 3) {
                //Cast to a Times Asked Filter Object
                TimesAskedFilter taf = (TimesAskedFilter) currFilter;

                //If nothing has been entered into the text box
                if (taf.editText.getText().toString().trim().equals("")) {
                    //Sets final return value to false since there is now an error
                    isNoError = false;

                    //Checks whether a number error has occurred
                    if (isNumberError) {
                        //Concatenate the Strings to show both a Number and Empty String Error
                        errorTV.setText("Please Enter a Valid Number for Times Asked and Please Fill All Text Fields");
                    }
                    else {
                        //Shows only the Text Error
                        errorTV.setText("Please Fill All Text Fields");
                    }

                    //Sets Error TextView to Visible
                    errorTV.setVisibility(View.VISIBLE);
                    //There has now been an empty text error, so this value is set to true
                    isTextError = true;
                }
                //If there is no text error, check if there is a number error
                else {
                    //Try to convert the text in the text field into a number
                    try {
                        Integer.parseInt(taf.editText.getText().toString());
                    }
                    catch (Exception e) {
                        //If there is an exception, it means that non-numerical characters were entered into the field
                        //Sets final return value to false since there is now an error
                        isNoError = false;

                        //Checks whether a text error has occurred
                        if (isTextError) {
                            //Concatenate the Strings to show both a Number and Empty String Error
                            errorTV.setText("Please Enter a Valid Number for Times Asked and Please Fill All Text Fields");

                        }
                        else {
                            //Shows only the Number Error
                            errorTV.setText("Please Enter a Valid Number for Times Asked");
                        }

                        //Sets Error TextView to Visible
                        errorTV.setVisibility(View.VISIBLE);
                        //There has now been a number  error, so this value is set to true
                        isNumberError = true;
                    }
                }
            }
        }

        return isNoError;
    }

    // Opens the Analysis Screen
    private void openAnalysis(ArrayList<Integer> timesAskedAL, ArrayList<Integer> numberCorrectAL)
    {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        Fragment fragment = new AnalysisFragment(timesAskedAL, numberCorrectAL);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentHolder, fragment);
        fragmentTransaction.addToBackStack(fragment.toString());
        fragmentTransaction.commit();
    }

    // Starts a Game
    private void openGame(String sql)
    {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        Fragment fragment = new Board1(isBot, sql);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentHolder, fragment);
        fragmentTransaction.addToBackStack(fragment.toString());
        fragmentTransaction.commit();
    }

    // Adds a Filter
    private void addFilterOption(String filterBy)
    {
        if (noFilters > 0)
        {
            addAndOr();
        }
        if ("Question".equals(filterBy))
        {
            addQuestionOption();
        } else if ("Subject and Topic".equals(filterBy))
        {
            addSubjectAndTopicOption();
        } else if ("Times Asked".equals(filterBy))
        {
            addTimesAskedOption();
        }

        noFilters++;
    }

    // Adds an And/Or Filter
    private void addAndOr()
    {
        Spinner spinner = new Spinner(getContext());
        String[] options = {"And", "Or"};
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, options);
        spinner.setAdapter(spinnerArrayAdapter);

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.addView(spinner);

        optionsContainer.addView(layout);

        filters.add(new AndOrFilter(spinner));
    }

    // Adds a Question Filter
    private void addQuestionOption() {
        // Create spinner and other UI elements dynamically
        // Then add them to optionsContainer
        Spinner spinner = new Spinner(getContext());

        TextView tv = new TextView(getContext());
        tv.setText("Question");
        tv.setTextColor(Color.WHITE);

        String[] options = {"Is", "Contains", "Begins With", "Ends With"};
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, options);
        spinner.setAdapter(spinnerArrayAdapter);

        EditText editText = new EditText(getContext());
        editText.setHint("                       ");
        editText.setTextColor(Color.WHITE);

        Button btnDeleteOption = new Button(getContext());
        btnDeleteOption.setText("-");
        btnDeleteOption.setId(View.generateViewId());
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(100, 100);
        btnDeleteOption.setLayoutParams(buttonParams);
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.OVAL);
        shape.setSize(10,10);
        shape.setColor(Color.RED); //change to the color you want
        btnDeleteOption.setBackground(shape);

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.addView(tv);
        layout.addView(spinner);
        layout.addView(editText);
        layout.addView(btnDeleteOption);

        optionsContainer.addView(layout);

        QuestionFilter qf = new QuestionFilter(spinner, editText);
        filters.add(qf);

        btnDeleteOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                qf.deleteAll();
                int index = filters.indexOf(qf);

                if (!(filters.size() == 1 || index == filters.size() - 1))
                {
                    filters.get(index + 1).deleteAll();
                    filters.remove(index + 1);
                }
                else if (index == filters.size() - 1 && filters.size() != 1)
                {
                    filters.get(index - 1).deleteAll();
                    filters.remove(index - 1);
                }

                filters.remove(qf);

                btnDeleteOption.setVisibility(View.GONE);
                tv.setVisibility(View.GONE);

                noFilters--;
            }
        });
    }

    // Adds a Subject/Topic Filter
    private void addSubjectAndTopicOption()
    {
        TextView tvSubject = new TextView(getContext());
        tvSubject.setText("Subject: ");

        TextView tvTopic = new TextView(getContext());
        tvTopic.setText("Topic: ");

        DBHandler dbHandler = new DBHandler(getContext());
        HashMap<String, Integer> subjects = dbHandler.getSubjects();
        ArrayList<String> optionsSubject2 = new ArrayList<>(Arrays.asList(dbHandler.getSubjects().keySet().toArray(new String[0])));
        optionsSubject2.add(0, "Any");
        //String[] optionsTopic = dbHandler.getTopics().keySet().toArray(new String[0]);
        String[] optionsTopic = {"Any"};

        Spinner spinnerSubject = new Spinner(getContext());
        ArrayAdapter<String> spinnerArrayAdapterSubject = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, optionsSubject2);
        spinnerSubject.setAdapter(spinnerArrayAdapterSubject);

        Spinner spinnerTopic = new Spinner(getContext());
        ArrayAdapter<String> spinnerArrayAdapterTopic = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, optionsTopic);
        spinnerTopic.setAdapter(spinnerArrayAdapterTopic);

        spinnerSubject.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                if (selectedItem.equals("Any"))
                {
                    ArrayList<String> optionsTopic2 = new ArrayList<>(Collections.singletonList("Any"));
                    ArrayAdapter<String> spinnerArrayAdapterTopic = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, optionsTopic2);
                    spinnerTopic.setAdapter(spinnerArrayAdapterTopic);
                }
                else
                {
                    int ID = subjects.get(selectedItem);
                    ArrayList<String> optionsTopic2 = new ArrayList<>(Arrays.asList(dbHandler.getTopics(ID).keySet().toArray(new String[0])));
                    optionsTopic2.add(0, "Any");
                    ArrayAdapter<String> spinnerArrayAdapterTopic = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, optionsTopic2);
                    spinnerTopic.setAdapter(spinnerArrayAdapterTopic);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //spinnerSubject.setBackgroundResource(R.drawable.button_border);
        //spinnerTopic.setBackgroundResource(R.drawable.button_border);
        tvSubject.setTextColor(Color.WHITE);
        tvTopic.setTextColor(Color.WHITE);

        Button btnDeleteOption = new Button(getContext());
        btnDeleteOption.setText("-");
        btnDeleteOption.setId(View.generateViewId());
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(100, 100);
        btnDeleteOption.setLayoutParams(buttonParams);
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.OVAL);
        shape.setSize(10,10);
        shape.setColor(Color.RED); //change to the color you want
        btnDeleteOption.setBackground(shape);

        LinearLayout layoutSubject = new LinearLayout(getContext());
        layoutSubject.setOrientation(LinearLayout.HORIZONTAL);
        layoutSubject.addView(tvSubject);
        layoutSubject.addView(spinnerSubject);

        LinearLayout layoutTopic = new LinearLayout(getContext());
        layoutTopic.setOrientation(LinearLayout.HORIZONTAL);
        layoutTopic.addView(tvTopic);
        layoutTopic.addView(spinnerTopic);

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(layoutSubject);
        layout.addView(layoutTopic);

        LinearLayout layoutMain = new LinearLayout(getContext());
        layoutMain.setOrientation(LinearLayout.HORIZONTAL);
        layoutMain.addView(layout);
        layoutMain.addView(btnDeleteOption);

        optionsContainer.addView(layoutMain);

        SubjectTopicFilter stf = new SubjectTopicFilter(spinnerSubject, spinnerTopic);
        filters.add(stf);

        btnDeleteOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                stf.deleteAll();
                int index = filters.indexOf(stf);

                if (!(filters.size() == 1 || index == filters.size() - 1))
                {
                    filters.get(index + 1).deleteAll();
                    filters.remove(index + 1);
                }
                else if (index == filters.size() - 1 && filters.size() != 1)
                {
                    filters.get(index - 1).deleteAll();
                    filters.remove(index - 1);
                }

                filters.remove(stf);

                btnDeleteOption.setVisibility(View.GONE);
                tvTopic.setVisibility(View.GONE);
                tvSubject.setVisibility(View.GONE);

                noFilters--;
            }
        });
    }

    // Adds a Times Asked Filter
    private void addTimesAskedOption()
    {
        // Similar to addQuestionOption, create the UI dynamically

        Spinner spinner = new Spinner(getContext());
        String[] options = {"Equal To", "Greater Than", "Lesser Than", "Greater Than or Equal To", "Lesser Than or Equal To"};
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, options);
        spinner.setAdapter(spinnerArrayAdapter);

        TextView tv = new TextView(getContext());
        tv.setText("Times Asked");
        tv.setTextColor(Color.WHITE);

        EditText editText = new EditText(getContext());
        editText.setHint("    ");
        editText.setTextColor(Color.WHITE);

        Button btnDeleteOption = new Button(getContext());
        btnDeleteOption.setText("-");
        btnDeleteOption.setId(View.generateViewId());
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(100, 100);
        btnDeleteOption.setLayoutParams(buttonParams);
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.OVAL);
        shape.setSize(10,10);
        shape.setColor(Color.RED); //change to the color you want
        btnDeleteOption.setBackground(shape);

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.addView(tv);
        layout.addView(spinner);
        layout.addView(editText);
        layout.addView(btnDeleteOption);

        optionsContainer.addView(layout);

        TimesAskedFilter taf = new TimesAskedFilter(spinner, editText);
        filters.add(taf);

        btnDeleteOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                taf.deleteAll();
                int index = filters.indexOf(taf);

                if (!(filters.size() == 1 || index == filters.size() - 1))
                {
                    filters.get(index + 1).deleteAll();
                    filters.remove(index + 1);
                }
                else if (index == filters.size() - 1 && filters.size() != 1)
                {
                    filters.get(index - 1).deleteAll();
                    filters.remove(index - 1);
                }

                filters.remove(taf);

                btnDeleteOption.setVisibility(View.GONE);
                tv.setVisibility(View.GONE);

                noFilters--;
            }
        });
    }

    public Filter(boolean isAnalyse, boolean isBot)
    {
        this.isAnalyse = isAnalyse;
        this.isBot = isBot;
    }
}

// Abstract Classes for specific FilterUI
abstract class FilterUI {
    //Used to identify the type of the filter
    int type;
    //Method to remove the UI if the delete button is clicked
    abstract public void deleteAll();
}

class QuestionFilter extends FilterUI {
    //Variables to store the UI Objects
    Spinner spinner;
    EditText editText;

    //Constructor for thr class
    public QuestionFilter(Spinner spinner, EditText editText) {
        //Used to identify the type of the filter
        type = 1;
        //Initialises UI objects
        this.spinner = spinner;
        this.editText = editText;
    }

    //Method to remove the UI if the delete button is clicked
    @Override
    public void deleteAll() {
        spinner.setVisibility(View.GONE);
        editText.setVisibility(View.GONE);
    }
}

class SubjectTopicFilter extends FilterUI {
    //Variables to store the UI Objects
    Spinner spinnerSubject;
    Spinner spinnerTopic;

    //Constructor for thr class
    public SubjectTopicFilter(Spinner spinnerSubject, Spinner spinnerTopic) {
        //Used to identify the type of the filter
        type = 2;
        //Initialises UI objects
        this.spinnerSubject = spinnerSubject;
        this.spinnerTopic = spinnerTopic;
    }

    //Method to remove the UI if the delete button is clicked
    @Override
    public void deleteAll() {
        spinnerSubject.setVisibility(View.GONE);
        spinnerTopic.setVisibility(View.GONE);
    }
}

class TimesAskedFilter extends FilterUI {
    //Variables to store the UI Objects
    Spinner spinner;
    EditText editText;

    //Constructor for thr class
    public TimesAskedFilter(Spinner spinner, EditText editText) {
        //Used to identify the type of the filter
        type = 3;
        //Initialises UI objects
        this.spinner = spinner;
        this.editText = editText;
    }

    //Method to remove the UI if the delete button is clicked
    @Override
    public void deleteAll() {
        spinner.setVisibility(View.GONE);
        editText.setVisibility(View.GONE);
    }
}

class AndOrFilter extends FilterUI {
    //Variables to store the UI Objects
    Spinner spinner;

    //Constructor for thr class
    public AndOrFilter(Spinner spinner) {
        //Used to identify the type of the filter
        type = 0;
        //Initialises UI objects
        this.spinner = spinner;
    }

    //Method to remove the UI if the delete button is clicked
    @Override
    public void deleteAll() {
        spinner.setVisibility(View.GONE);
    }
}

