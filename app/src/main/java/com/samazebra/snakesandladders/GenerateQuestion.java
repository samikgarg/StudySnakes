package com.samazebra.snakesandladders;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GenerateQuestion extends DialogFragment
{
    interface GptCallback {
        void onResult(String result);
    }

    // Sets Member Variables
    EditQuestions parent;
    EditText description;
    String response;
    String apiKey = BuildConfig.API_KEY;

    // Runs as soon as layout is loaded
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.generate_question, null);

        description = view.findViewById(R.id.description);

        builder.setView(view)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Generate", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        generateQuestion(description.getText().toString(), new GptCallback()
                        {
                            @Override
                            public void onResult(String result)
                            {
                                parent.setGeneration(response);
                            }
                        });
                        dismiss();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        dialog.setOnShowListener(new DialogInterface.OnShowListener()
        {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button buttonPositive = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                Button buttonNegative = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);

                buttonPositive.setTextColor(Color.WHITE);
                buttonNegative.setTextColor(Color.WHITE);

                ViewGroup parent2 = (ViewGroup) buttonPositive.getParent();
                parent2.setBackgroundResource(R.drawable.button_border);

                buttonPositive.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        generateQuestion(description.getText().toString(), new GptCallback()
                        {
                            @Override
                            public void onResult(String result)
                            {
                                parent.setGeneration(response);
                                Log.i("RESPONSE", response);
                            }
                        });
                        dismiss();
                    }
                });
            }
        });

        return dialog;
    }

    // Sets the parent layout
    public void setParent (EditQuestions parent)
    {
        this.parent = parent;
    }

    // Generates the Question
    public void generateQuestion(final String description, final GptCallback callback) {
        // Get the ConnectivityManager to check network status
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();

        // Check network connectivity and log the result
        if (ni == null) {
            Log.e("Network Error", "No network");
        } else {
            Log.i("Success", "Network connected");
        }

        // Create a new thread to handle the network request
        new Thread(new Runnable() {
            public void run() {
                HttpURLConnection con = null;
                try {
                    Log.i("HERE 1", "HERE 1");
                    // Initialize the API URL
                    String url = "https://api.openai.com/v1/chat/completions";
                    con = (HttpURLConnection) new URL(url).openConnection();

                    Log.i("HERE 3", "HERE 3 " + con);

                    // Set the HTTP request method and headers
                    con.setRequestMethod("POST");
                    con.setRequestProperty("Content-Type", "application/json");
                    con.setRequestProperty("Authorization", apiKey);

                    // Create a JSON object to hold the request payload
                    JSONObject data = new JSONObject();
                    data.put("model", "gpt-3.5-turbo");
                    data.put("max_tokens", 3800);
                    data.put("temperature", 1.0);

                    // Create an array of messages to be sent to the API
                    JSONArray messages = new JSONArray();

                    // System message to set the behavior of the assistant
                    JSONObject systemMessage = new JSONObject();
                    systemMessage.put("role", "system");
                    systemMessage.put("content", "You are a helpful assistant.");
                    messages.put(systemMessage);

                    // User message containing the task description
                    JSONObject userMessage = new JSONObject();
                    userMessage.put("role", "user");
                    userMessage.put("content", "Given the description: \"" + description.trim() + "\", generate a multiple-choice question. The format is: [Question];[Options in commas];[Correct option's numerical position, beginning with 1]. There must be two to six options in total. Ensure the answer's position matches the provided number. E.g., if the second option is correct, the number should be '2'. Validate the answer's accuracy.");  // trimmed for brevity
                    messages.put(userMessage);

                    // Add the messages array to the main JSON object
                    data.put("messages", messages);

                    // Send the POST data
                    con.setDoOutput(true);
                    con.getOutputStream().write(data.toString().getBytes());

                    // Read the API response
                    int responseCode = con.getResponseCode();
                    BufferedReader reader;
                    if (200 <= responseCode && responseCode <= 299) {
                        reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    } else {
                        reader = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                    }

                    Log.i("HERE 4", "HERE 4 " + responseCode);
                    Log.i("HERE 5", "HERE 5 " + reader);

                    // Extract the response string
                    String output = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        output = reader.lines().reduce((a, b) -> a + b).get();
                        Log.i("HERE 6", "HERE 6 " + output);

                    }

                    // Parse the JSON response
                    JSONObject jsonResponse = new JSONObject(output);

                    // Check for "choices" in the response
                    if (jsonResponse.has("choices")) {
                        String responseText = jsonResponse.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
                        Log.i("HERE 7", "HERE 7 " + responseText);
                        response = responseText;

                        // Invoke callback with the generated question
                        callback.onResult(responseText);
                    } else {
                        // Log the entire response if "choices" is not present
                        Log.e("Choices not present", jsonResponse.toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i("HERE 2", "HERE 2");
                } finally {
                    // Disconnect the connection
                    if (con != null) {
                        con.disconnect();
                    }
                }
            }
        }).start();
    }

}
