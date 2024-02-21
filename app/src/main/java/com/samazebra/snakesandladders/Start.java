package com.samazebra.snakesandladders;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class Start extends Fragment
{
    // Runs as soon as layout is launched and initialises all layout elements
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Links the layout file so that it can be displayed on the screen
        View v = inflater.inflate(R.layout.start, container, false);

        // Variables for all Layout Elements
        Button botButton = v.findViewById(R.id.bot_button);
        Button playerButton = v.findViewById(R.id.player_button);
        Button editQuestions = v.findViewById(R.id.edit_questions);
        Button analysePerformance = v.findViewById(R.id.analyse_performance);

        // Sets the action to be performed when the Analyse Performance button is clicked
        analysePerformance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                Fragment fragment = new Filter(true, false);
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragmentHolder, fragment);
                fragmentTransaction.addToBackStack(fragment.toString());
                fragmentTransaction.commit();
            }
        });

        // Sets the action to be performed when the Edit Questions button is clicked
        editQuestions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                Fragment fragment = new QuestionsSearch();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragmentHolder, fragment);
                fragmentTransaction.addToBackStack(fragment.toString());
                fragmentTransaction.commit();
            }
        });

        // Sets the action to be performed when the Play with Bot button is clicked
        botButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                Fragment fragment = new Filter(false, true);
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragmentHolder, fragment);
                fragmentTransaction.addToBackStack(fragment.toString());
                fragmentTransaction.commit();
            }
        });

        // Sets the action to be performed when the Play with Another Player button is clicked
        playerButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                Fragment fragment = new Filter(false, false);
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragmentHolder, fragment);
                fragmentTransaction.addToBackStack(fragment.toString());
                fragmentTransaction.commit();
            }
        });

        return v;
    }
}
