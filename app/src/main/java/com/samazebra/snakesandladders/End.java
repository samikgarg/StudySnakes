package com.samazebra.snakesandladders;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class End extends Fragment
{
    // Member Variables
    TextView tv;
    public static String text;
    public static int color;
    private Button replay;
    private Button exit;
    private String sql;

    // Runs when laout loads
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        View v = inflater.inflate(R.layout.end, container, false);

        tv = v.findViewById(R.id.win_text);
        tv.setText(text);
        tv.setTextColor(color);

        replay = v.findViewById(R.id.replay);
        exit = v.findViewById(R.id.exit);

        replay.setTextColor(color);
        exit.setTextColor(color);

        replay.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                Fragment fragment = new Board1(Board.isBot, sql);
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragmentHolder, fragment);
                fragmentTransaction.addToBackStack(fragment.toString());
                fragmentTransaction.commit();
            }
        });

        exit.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                Fragment fragment = new Start();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragmentHolder, fragment);
                fragmentTransaction.addToBackStack(fragment.toString());
                fragmentTransaction.commit();
            }
        });

        return v;
    }

    // Constructor
    public End (String sql)
    {
        this.sql = sql;
    }
}
