package com.samazebra.snakesandladders;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.widget.ImageView;

public class Player
{
    // Meember Variables
    private ImageView iv;
    private Square square;
    private Board board;
    private int playerNo;
    private boolean isBot;
    private Player otherPlayer;
    final private int timePerSquare = 200;
    private int x;
    private int y;

    // Constructor
    public Player(ImageView iv, Board board, int playerNo, boolean isBot)
    {
        this.iv = iv;
        this.board = board;
        square = board.squares[0];
        this.playerNo = playerNo;
        this.isBot = isBot;
    }

    // Getters/Setters
    public boolean getIsBot ()
    {
        return isBot;
    }

    public void setOtherPlayer (Player otherPlayer)
    {
        this.otherPlayer = otherPlayer;
    }

    public int getPlayerNo()
    {
        return playerNo;
    }

    public ImageView getIv()
    {
        return iv;
    }

    public Square getSquare()
    {
        return square;
    }

    // Moves the Player on the board
    public void movePlayer(Square square, boolean isStart, Square[] squares, boolean isEnd, Square end)
    {
        ObjectAnimator animationX;
        ObjectAnimator animationY;


        ObjectAnimator animationX1;
        ObjectAnimator animationX2;
        ObjectAnimator animationX3;

        ObjectAnimator animationY1;
        ObjectAnimator animationY2;
        ObjectAnimator animationY3;

        Player player = this;

        int duration;
        int duration1;
        int duration2;
        int duration3;

        Square prevSquare;

        Board.isAnimationEnd = false;

        if (isStart)
        {
            // Sets the position of the players to the first square without any animation
            iv.setX(square.getX());
            iv.setY(square.getY());

            // Boolean to indicate that the animation has ended
            Board.isAnimationEnd = true;

            // Adds the players to the first square
            square.addPlayers(this);
        }
        else if (isEnd)
        {
            // Variable to store the current square
            prevSquare = this.square;
            // Removes the player from the current square
            prevSquare.removePlayers(this);

            // There are three total animations: moving to the edge, moving up by 1, and moving to the resulting square
            // Calculates the duration for each animation as the number of squares travelled multiplied by the constant time per square
            duration1 = (end.getSquareNo() - prevSquare.getSquareNo()) * timePerSquare;
            duration2 = (squares[end.getSquareNo()].getSquareNo() - end.getSquareNo()) * timePerSquare;
            duration3 = (square.getSquareNo() - squares[end.getSquareNo()].getSquareNo()) * timePerSquare;

            // Initialises all of the animations
            animationX1 = ObjectAnimator.ofFloat(iv, "translationX", end.getX());
            animationX2 = ObjectAnimator.ofFloat(iv, "translationX", squares[end.getSquareNo()].getX());
            animationX3 = ObjectAnimator.ofFloat(iv, "translationX", square.getX());

            animationY1 = ObjectAnimator.ofFloat(iv, "translationY", end.getY());
            animationY2 = ObjectAnimator.ofFloat(iv, "translationY", squares[end.getSquareNo()].getY());
            animationY3 = ObjectAnimator.ofFloat(iv, "translationY", square.getY());

            // Sets the duration for all of the animations
            animationX1.setDuration(duration1);
            animationX2.setDuration(duration2);
            animationX3.setDuration(duration3);

            animationY1.setDuration(duration1);
            animationY2.setDuration(duration2);
            animationY3.setDuration(duration3);

            // Runs the first animation
            animationX1.start();
            animationY1.start();

            // Adds listeners to the first set of animations for actions to be performed after the animation
            animationX1.addListener(new AnimatorListenerAdapter()
            {
                @Override
                public void onAnimationEnd(Animator animation)
                {
                    animationY1.addListener(new AnimatorListenerAdapter()
                    {
                        @Override
                        public void onAnimationEnd(Animator animation)
                        {
                            // Starts the second set of animations once the first set has finished
                            animationX2.start();
                            animationY2.start();

                            // Adds listeners to the second set of animations
                            animationX2.addListener(new AnimatorListenerAdapter()
                            {
                                @Override
                                public void onAnimationEnd(Animator animation)
                                {
                                    animationY2.addListener(new AnimatorListenerAdapter()
                                    {
                                        @Override
                                        public void onAnimationEnd(Animator animation)
                                        {
                                            // Starts the third set if animations
                                            animationX3.start();
                                            animationY3.start();

                                            //Adds listeners for the third set of animations
                                            animationX3.addListener(new AnimatorListenerAdapter()
                                            {
                                                @Override
                                                public void onAnimationEnd(Animator animation)
                                                {
                                                    animationY3.addListener(new AnimatorListenerAdapter()
                                                    {
                                                        @Override
                                                        public void onAnimationEnd(Animator animation)
                                                        {
                                                            //All animations have now finished
                                                            // Adds the player to the final square
                                                            square.addPlayers(player);
                                                            // This method checks if the game has ended and shows the end screen if it has
                                                            checkMove(squares, player);
                                                            // Asks a question if the move was not performed by a bot
                                                            board.ask(isBot);

                                                            // Code to check if the player landed on a Snake or Ladder
                                                            // Moves the player to the appropriate square

                                                            // If the player lands on a square containing a Snake or Ladder
                                                            // A type of 0 indicates that the square is a regular square, meaning it does not contain a snake or ladder
                                                            if (square.getType() != 0) {
                                                                // Get the corresponding final square
                                                                Square corrSquare = square.getCorrSquare();
                                                                // Remove the player from the current square
                                                                square.removePlayers(player);

                                                                // Get the numbers of the current and final squares
                                                                double currNo = square.getSquareNo();
                                                                double corrNo = corrSquare.getSquareNo();

                                                                // Determine the row numbers of the current and final squares
                                                                double rowCurrSquare = Math.ceil(currNo / 9);
                                                                double rowCorrSquare = Math.ceil(corrNo / 9);

                                                                // Determine the row numbers of the current square
                                                                double colCurrSquare;
                                                                if (rowCurrSquare % 2 == 0) {
                                                                    colCurrSquare = 10 - currNo + 9 * (rowCurrSquare - 1);
                                                                } else {
                                                                    colCurrSquare = currNo - 9 * (rowCurrSquare - 1);
                                                                }

                                                                // Determine the row numbers of the final square
                                                                double colCorrSquare;
                                                                if (rowCorrSquare % 2 == 0) {
                                                                    colCorrSquare = 10 - corrNo + 9 * (rowCorrSquare - 1);
                                                                } else {
                                                                    colCorrSquare = corrNo - 9 * (rowCorrSquare - 1);
                                                                }

                                                                // Use Pythagorean Theorem to calculate the distance between the current and final square in terms of the number of squares
                                                                double length = Math.sqrt((colCorrSquare - colCurrSquare)*(colCorrSquare - colCurrSquare) + (rowCorrSquare - rowCurrSquare)*(rowCorrSquare - rowCurrSquare));

                                                                // Calculate the duration of the animation by multiplying the length by the constant time per square
                                                                int durationC = (int) Math.round(length * timePerSquare);

                                                                // Set up and execute the animations
                                                                ObjectAnimator animationCX = ObjectAnimator.ofFloat(iv, "translationX", corrSquare.getX());
                                                                ObjectAnimator animationCY = ObjectAnimator.ofFloat(iv, "translationY", corrSquare.getY());
                                                                animationCX.setDuration(durationC);
                                                                animationCY.setDuration(durationC);
                                                                animationCX.start();
                                                                animationCY.start();

                                                                // Adds a listener to the animation for actions to be performed after the animation
                                                                animationCX.addListener(new AnimatorListenerAdapter() {
                                                                    @Override
                                                                    public void onAnimationEnd(Animator animation) {
                                                                        animationCY.addListener(new AnimatorListenerAdapter() {
                                                                            @Override
                                                                            public void onAnimationEnd(Animator animation) {
                                                                                // Adds the player to the final square
                                                                                corrSquare.addPlayers(player);
                                                                                // Indicates that the animation has ended
                                                                                Board.isAnimationEnd = true;
                                                                                super.onAnimationEnd(animation);
                                                                            }
                                                                        });
                                                                        super.onAnimationEnd(animation);
                                                                    }
                                                                });
                                                            }
                                                            super.onAnimationEnd(animation);
                                                        }
                                                    });
                                                    super.onAnimationEnd(animation);
                                                }
                                            });
                                            super.onAnimationEnd(animation);
                                        }
                                    });
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
        else
        {
            // Variable to store the current square
            prevSquare = this.square;
            // Removes the player from the current square
            prevSquare.removePlayers(this);

            // Calculates the duration of the animation
            // This is the number of squares to travel multiplied by the time per square
            duration = (square.getSquareNo() - prevSquare.getSquareNo()) * timePerSquare;

            // Sets and starts the animations
            animationX = ObjectAnimator.ofFloat(iv, "translationX", square.getX());
            animationY = ObjectAnimator.ofFloat(iv, "translationY", square.getY());
            animationX.setDuration(duration);
            animationX.start();
            animationY.setDuration(duration);
            animationY.start();

            // Adds a listener to both animations for actions to be performed after the animation
            animationX.addListener(new AnimatorListenerAdapter()
            {
                @Override
                public void onAnimationEnd(Animator animation)
                {
                    animationY.addListener(new AnimatorListenerAdapter()
                    {
                        @Override
                        public void onAnimationEnd(Animator animation)
                        {
                            // Adds the player to the final square
                            square.addPlayers(player);
                            // This method checks if the game has ended and shows the end screen if it has
                            checkMove(squares, player);
                            // Asks a question if the move was not performed by a bot
                            board.ask(isBot);

                            // Code to check if the player landed on a Snake or Ladder
                            // Moves the player to the appropriate square
                            if (square.getType() != 0)
                            {
                                Square corrSquare = square.getCorrSquare();
                                square.removePlayers(player);

                                double currNo = square.getSquareNo();
                                double corrNo = corrSquare.getSquareNo();

                                double rowCurrSquare = Math.ceil(currNo / 9);
                                double rowCorrSquare = Math.ceil(corrNo / 9);

                                double colCurrSquare;
                                double colCorrSquare;

                                if (rowCurrSquare % 2 == 0)
                                {
                                    colCurrSquare = 10 - currNo + 9 * (rowCurrSquare - 1);
                                }
                                else
                                {
                                    colCurrSquare = currNo - 9 * (rowCurrSquare - 1);
                                }

                                if (rowCorrSquare % 2 == 0)
                                {
                                    colCorrSquare = 10 - corrNo + 9 * (rowCorrSquare - 1);
                                }
                                else
                                {
                                    colCorrSquare = corrNo - 9 * (rowCorrSquare - 1);
                                }

                                double length = Math.sqrt((colCorrSquare - colCurrSquare)*(colCorrSquare - colCurrSquare) + (rowCorrSquare - rowCurrSquare)*(rowCorrSquare - rowCurrSquare));
                                int durationC = (int) Math.round(length * timePerSquare);

                                ObjectAnimator animationCX = ObjectAnimator.ofFloat(iv, "translationX", corrSquare.getX());
                                ObjectAnimator animationCY = ObjectAnimator.ofFloat(iv, "translationY", corrSquare.getY());
                                animationCX.setDuration(durationC);
                                animationCY.setDuration(durationC);
                                animationCX.start();
                                animationCY.start();

                                animationCX.addListener(new AnimatorListenerAdapter()
                                {
                                    @Override
                                    public void onAnimationEnd(Animator animation)
                                    {
                                        animationCY.addListener(new AnimatorListenerAdapter()
                                        {
                                            @Override
                                            public void onAnimationEnd(Animator animation)
                                            {
                                                corrSquare.addPlayers(player);
                                                Board.isAnimationEnd = true;
                                                super.onAnimationEnd(animation);
                                            }
                                        });
                                        super.onAnimationEnd(animation);
                                    }
                                });
                            }
                            super.onAnimationEnd(animation);
                        }
                    });
                    super.onAnimationEnd(animation);
                }
            });

        }

        if (square.getType() == 0)
        {
            this.square = square;
        }
        else
        {
            this.square = square.getCorrSquare();
        }

        iv.bringToFront();
    }

    // Checks the outcome of a move
    private void checkMove(Square[] squares, Player player)
    {
        if (square == squares[89])
        {
            endGame(player);
        }

        if(square.getType() == 0)
        {
            Board.isAnimationEnd = true;
        }
    }

    // Ends the Game
    private void endGame(Player player)
    {
        if (isBot || otherPlayer.getIsBot())
        {
            if (!isBot)
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

        board.win();
    }

}
