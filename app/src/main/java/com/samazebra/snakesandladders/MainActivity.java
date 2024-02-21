package com.samazebra.snakesandladders;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.os.Bundle;
public class MainActivity extends AppCompatActivity
{
    // Starts the operation of the app when it is launched
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Sets the Fragment Home Screen as the Layout
        FragmentManager fragmentManager = getSupportFragmentManager();

        Fragment fragment = fragmentManager.findFragmentById (R.id.fragmentHolder);

        if (fragment == null)
        {
            fragment = new Start();

            fragmentManager.beginTransaction()
                    .add (R.id.fragmentHolder, fragment)
                    .commit();
        }
    }
}