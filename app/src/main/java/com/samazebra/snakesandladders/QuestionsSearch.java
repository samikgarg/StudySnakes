package com.samazebra.snakesandladders;

import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class QuestionsSearch extends Fragment implements AdapterView.OnItemClickListener {

    // All Member Variables
    private DBHandler dbHandler;
    private SimpleCursorAdapter adapter;
    public String searchString;

    // Runs as soon as layout is loaded
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.questions_search, container, false);

        dbHandler = new DBHandler(getActivity());
        ListView listView_questions = view.findViewById(R.id.listView_questions);
        EditText editText_searchField = view.findViewById(R.id.editText_searchField);

        listView_questions.setOnItemClickListener(this);

        String[] columns = new String[]{
                DBHandler.QUESTION_NAME_COL,
                DBHandler.TOPIC_NAME_COL,
                DBHandler.SUBJECT_NAME_COL
        };

        int[] boundTo = new int[]{
                R.id.text1,
                R.id.text2,
                R.id.text3
        };

        adapter = new SimpleCursorAdapter(
                getActivity(), R.layout.list_item,
                null, columns, boundTo,
                0);

        listView_questions.setAdapter(adapter);

        editText_searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchString = s.toString();
                updateList(searchString);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        FloatingActionButton btnAdd = view.findViewById(R.id.btn_add);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditQuestions editQuestionsDialog = new EditQuestions();
                editQuestionsDialog.setParent(QuestionsSearch.this);
                editQuestionsDialog.show(getChildFragmentManager(), "edit_questions_tag");
            }
        });

        searchString = "";
        updateList(searchString);

        return view;
    }

    // Updates the List of Questions shown on screen
    public void updateList(String filter) {
        Cursor newCursor = dbHandler.search(filter);
        adapter.changeCursor(newCursor);
    }

    // Handles when an item is clicked
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        DBHandler handler = new DBHandler(getActivity());

        EditQuestions editQuestionsDialog = new EditQuestions();
        editQuestionsDialog.setParent(QuestionsSearch.this);
        Cursor cursor = (Cursor) adapter.getItem(position);
        int idIndex = cursor.getColumnIndexOrThrow(DBHandler.QUESTIONS_ID_COL);
        int questionId = Math.toIntExact(cursor.getLong(idIndex));
        editQuestionsDialog.setData(handler.getData(questionId), questionId);
        editQuestionsDialog.show(getChildFragmentManager(), "edit_questions_tag");
    }
}