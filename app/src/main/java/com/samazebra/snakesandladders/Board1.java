package com.samazebra.snakesandladders;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class Board1 extends Board
{
    // Stores if Game is against Bot or another player
    private boolean isBot;

    // Runs as soon as layout loads
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        setLayout(R.layout.board1);
        setDice1ImageView(R.id.dice1_1);
        setDice2ImageView(R.id.dice1_2);

        super.onCreate(savedInstanceState);

        View v = inflater.inflate(getLayout(), container, false);

        Log.i("isBot", "" + isBot);

        isTurn1 = true;
        isTurn2 = false;

        player1 = new Player(v.findViewById(R.id.player1), this, 1, false);
        player2 = new Player(v.findViewById(R.id.player2), this, 2, isBot);

        player1.setOtherPlayer(player2);
        player2.setOtherPlayer(player1);


        dice1 = v.findViewById(getDice1ImageView());
        dice2 = v.findViewById(getDice2ImageView());

        arrow1 = v.findViewById(R.id.arrow1);
        arrow2 = v.findViewById(R.id.arrow2);

        arrow1.setVisibility(View.VISIBLE);
        arrow2.setVisibility(View.INVISIBLE);

        animateArrow1();
        animateArrow2();


        getActivity().getWindow().getDecorView().post(new Runnable()
        {

            @Override
            public void run()
            {
                setSquares(v);
                player2.movePlayer(squares[0], true, squares, false, squares[0]);
                player1.movePlayer(squares[0], true, squares, false, squares[0]);
            }

        });


        dice1.setOnClickListener (v12 -> {
            if (isBot)
            {
                if (Board.isRollDice && Board.isTurn1 && Board.isAnimationEnd)
                {
                    rollDice(dice1, player1, true, player2, dice2, false);
                }
            }
            else
            {
                if (Board.isRollDice && Board.isTurn1 && Board.isAnimationEnd)
                {
                    rollDice(dice1, player1, false, player2, dice2, false);
                }
            }
        });

        if (!isBot)
        {
            dice2.setOnClickListener (v1 -> {
                if (Board.isRollDice && Board.isTurn2 && Board.isAnimationEnd)
                {
                    rollDice(dice2, player2, false, player1, dice1, false);
                }

            });
        }

        Timer mTimer = new Timer();
        mTimer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                getActivity().runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if (Board.isAnimationEnd)
                        {
                            ArrayList<Player> players = squares[89].getPlayers();

                            if (players.size() > 0)
                            {
                                Player player = players.get(0);

                                if (isBot)
                                {
                                    if (player.getPlayerNo() == 1)
                                    {
                                        End.color = Color.BLUE;
                                        End.text = "YOU WON";
                                    }
                                    else
                                    {
                                        End.color = Color.RED;
                                        End.text = "YOU LOSE";
                                    }
                                }
                                else
                                {
                                    if (player.getPlayerNo() == 1)
                                    {
                                        End.color = Color.BLUE;
                                        End.text = "BLUE WON";
                                    }
                                    else
                                    {
                                        End.color = Color.RED;
                                        End.text = "RED WON";
                                    }
                                }
                            }

                            cancel();
                        }
                    }
                });
            }
        }, 0, 999999999);

        return v;
    }

    // Initialises the Squares in the Layout
    public void setSquares(View v)
    {
        for(int i = 1 ; i <= squares.length ; i++)
        {
            String currNo;

            if (i < 10)
            {
                currNo = "0" + i;
            }
            else
            {
                currNo = "" + i;
            }


            String id = "square" + currNo;
            int idInt = getResources().getIdentifier(id, "id", getActivity().getPackageName());

            squares[i - 1] = new Square(v.findViewById(idInt), i);
            squares[i - 1].setType(0);
        }

        squares[5].setCorrSquare(squares[42]);
        squares[5].setType(1);

        squares[19].setCorrSquare(squares[70]);
        squares[19].setType(1);

        squares[31].setCorrSquare(squares[49]);
        squares[31].setType(1);

        squares[43].setCorrSquare(squares[2]);
        squares[43].setType(2);

        squares[63].setCorrSquare(squares[83]);
        squares[63].setType(1);

        squares[84].setCorrSquare(squares[18]);
        squares[84].setType(2);

        squares[88].setCorrSquare(squares[45]);
        squares[88].setType(2);
    }

    // Constructor to set Data
    public Board1(boolean isBot, String sql)
    {
        this.isBot = isBot;
        Board.isBot = isBot;

        this.sql = sql;
    }

    // Animates Arrow 1
    private void animateArrow1()
    {
        ObjectAnimator animation11 = ObjectAnimator.ofFloat(arrow1, "translationX", arrow1.getX() - 50);
        ObjectAnimator animation12 = ObjectAnimator.ofFloat(arrow1, "translationX", arrow1.getX());

        animation11.setDuration(500);
        animation12.setDuration(500);

        animation11.start();

        animation11.addListener(new AnimatorListenerAdapter()
        {
            @Override
            public void onAnimationEnd(Animator animation)
            {
                animation12.start();

                animation12.addListener(new AnimatorListenerAdapter()
                {
                    @Override
                    public void onAnimationEnd(Animator animation)
                    {
                        animation11.start();

                        animation11.addListener(new AnimatorListenerAdapter()
                        {
                            @Override
                            public void onAnimationEnd(Animator animation)
                            {
                                animation12.start();
                                super.onAnimationEnd(animation);
                            }
                        });
                        super.onAnimationEnd(animation);
                    }
                });
                super.onAnimationEnd(animation);
            }
        });
    }

    // Animates Arrow 2
    public void animateArrow2 ()
    {
        ObjectAnimator animation11 = ObjectAnimator.ofFloat(arrow2, "translationX", arrow2.getX() - 50);
        ObjectAnimator animation12 = ObjectAnimator.ofFloat(arrow2, "translationX", arrow2.getX());

        animation11.setDuration(500);
        animation12.setDuration(500);

        animation11.start();

        animation11.addListener(new AnimatorListenerAdapter()
        {
            @Override
            public void onAnimationEnd(Animator animation)
            {
                animation12.start();

                animation12.addListener(new AnimatorListenerAdapter()
                {
                    @Override
                    public void onAnimationEnd(Animator animation)
                    {
                        animation11.start();

                        animation11.addListener(new AnimatorListenerAdapter()
                        {
                            @Override
                            public void onAnimationEnd(Animator animation)
                            {
                                //animation12.start();
                                super.onAnimationEnd(animation);
                            }
                        });
                        super.onAnimationEnd(animation);
                    }
                });
                super.onAnimationEnd(animation);
            }
        });
    }

}
