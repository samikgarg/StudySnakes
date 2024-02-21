package com.samazebra.snakesandladders;

import android.view.View;
import android.widget.ImageView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class Board extends Fragment
{
    //Member Variables
    Square[] squares = new Square[90];
    Random random = new Random();

    public static boolean isRollDice = true;
    public static boolean isTurn1 = true;
    public static boolean isTurn2 = false;
    public static boolean isFinished = false;
    public static boolean isAnimationEnd = true;
    public static boolean isBot;

    private int layout;
    private int dice1ImageView;
    private int dice2ImageView;

    public Player player1;
    public Player player2;
    String sql;

    ImageView dice1;
    ImageView dice2;

    ImageView arrow1;
    ImageView arrow2;

    // Getter/Setter Methods
    public void setLayout(int layout)
    {
        this.layout = layout;
    }
    public void setDice1ImageView(int dice1ImageView)
    {
        this.dice1ImageView = dice1ImageView;
    }
    public void setDice2ImageView(int dice2ImageView)
    {
        this.dice2ImageView = dice2ImageView;
    }
    public int getLayout()
    {
        return layout;
    }
    public int getDice1ImageView()
    {
        return dice1ImageView;
    }
    public int getDice2ImageView()
    {
        return dice2ImageView;
    }

    int [] dice1Array =
            {
                    R.drawable.dice1, R.drawable.dice2, R.drawable.dice3, R.drawable.dice4, R.drawable.dice5, R.drawable.dice6
            };


    // Function to roll a dice
    public void rollDice(ImageView dice, Player player, boolean isBot, Player player2, ImageView dice2, boolean isBotPlaying) {
        // Initialize flag to indicate whether the dice can be rolled
        // Ensures that the dice cannot be rolled during a dice or move animation
        isRollDice = false;

        // Generate a random number between 1 and 6 for the dice roll
        int diceRoll = random.nextInt(6) + 1;

        // Initialize variables to keep track of the dice animation
        int[] position = {1};
        int[] noTimes = {0};

        // Initialize a Timer for controlling the dice animation
        Timer mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                // Run code on the UI thread for updating UI elements
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Hide the arrows that indicate whose turn it is
                        arrow1.setVisibility(View.INVISIBLE);
                        arrow2.setVisibility(View.INVISIBLE);

                        // Increment and reset the animation counter if it exceeds the length of the dice image array
                        position[0]++;
                        if (position[0] >= dice1Array.length) {
                            position[0] = 0;
                        }

                        // Set the dice image for the current animation cycle
                        dice.setImageResource(dice1Array[position[0]]);

                        // Check if we've gone through 3 cycles of dice animation (18 frames)
                        if (noTimes[0] > 18) {
                            // Reset animation frame counter
                            noTimes[0] = 0;

                            // Set the final dice image based on the randomly generated roll
                            dice.setImageResource(dice1Array[diceRoll - 1]);

                            // Set the flag to indicate that the dice roll is complete
                            isRollDice = true;

                            // Handle game logic based on the player number and dice roll
                            // The player gets a second turn if they roll a 6
                            // Otherwise, it moves on to the next player
                            if (player.getPlayerNo() == 1) {
                                if (diceRoll == 6) {
                                    isTurn1 = true;
                                    isTurn2 = false;
                                } else {
                                    isTurn1 = false;
                                    isTurn2 = true;
                                }
                            } else {
                                if (diceRoll == 6) {
                                    isTurn2 = true;
                                    isTurn1 = false;
                                } else {
                                    isTurn2 = false;
                                    isTurn1 = true;
                                }
                            }

                            // Flag to indicate that we're done making game decisions for this roll
                            isFinished = true;

                            // Create a new Timer to update UI elements like arrows indicating turns
                            Timer Timer = new Timer();
                            Timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            // Show the arrow that corresponds to the player whose turn it is if all animations have ended
                                            if (isAnimationEnd) {
                                                if (isTurn1) {
                                                    arrow1.setVisibility(View.VISIBLE);
                                                } else if (isTurn2 && !isBot) {
                                                    arrow2.setVisibility(View.VISIBLE);
                                                }
                                                cancel();
                                            }
                                        }
                                    });
                                }
                            }, 0, 100);

                            // Handle player movement on the board based on the dice roll
                            // Only move the player if their dice roll does not take them beyond the final (90th) Square
                            if (player.getSquare().getSquareNo() + diceRoll - 1 < 90) {
                                // Boolean to store the result of whether the player will encounter an edge
                                boolean isEnd = false;
                                // Calculate the square that the player needs to travel to
                                Square currSquare = player.getSquare();
                                Square moveSquare = squares[currSquare.getSquareNo() + diceRoll - 1];

                                // Check if the player will encounter an edge and move the player accordingly
                                for (int i = currSquare.getSquareNo(); i < moveSquare.getSquareNo(); i++) {
                                    if (i % 9 == 0) {
                                        player.movePlayer(moveSquare, false, squares, true, squares[i - 1]);
                                        isEnd = true;
                                    }
                                }

                                // Move the player accordingly if no edge will be encountered
                                if (!isEnd) {
                                    player.movePlayer(moveSquare, false, squares, false, squares[0]);
                                }
                            }

                            // Move the bot again if it rolls a 6
                            if (isBotPlaying && isTurn2 && isRollDice) {
                                Timer mTimer = new Timer();
                                mTimer.schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (isAnimationEnd) {
                                                    rollDice(dice, player, false, player2, dice2, true);
                                                    isRollDice = false;
                                                    cancel();
                                                }
                                            }
                                        });
                                    }
                                }, 0, 100);
                            }

                            // Cancel the TimerTask, effectively stopping the dice animation
                            cancel();
                        }

                        // Increment the frame counter for the next animation cycle
                        noTimes[0]++;
                    }
                });
            }
        }, 0, 100); // 0 delay, 100 ms for each task
    }


    // Moves the Bot
    public void moveBot(Player player, Player player2, ImageView dice)
    {
        getActivity().runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if (isAnimationEnd)
                {
                    rollDice(dice2, player2, false, player, dice, true);
                    isRollDice = false;
                }
            }
        });
    }

    // Ends the Game
    public void win()
    {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        Fragment fragment = new End(sql);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentHolder, fragment);
        fragmentTransaction.addToBackStack(fragment.toString());
        fragmentTransaction.commit();
    }

    // Asks a Question
    public void ask (boolean isBot)
    {
        if (!isBot)
        {
            QuestionDialogFragment dialog = new QuestionDialogFragment(sql);
            dialog.setDetails(this);
            dialog.show(getActivity().getSupportFragmentManager(), "QuestionDialog");
        }
    }
}
