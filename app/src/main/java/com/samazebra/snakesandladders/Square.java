package com.samazebra.snakesandladders;

import android.content.res.Resources;
import android.view.ViewGroup;
import android.widget.ImageView;
import java.util.ArrayList;

public class Square
{
    // Member Variables
    private float x;
    private float y;
    private Square corrSquare;
    private int squareNo;
    private ImageView iv;
    // Normal is 0, Ladder is 1, Snake is 2
    private int type = 0;
    private ArrayList<Player> players = new ArrayList<>();

    // Constructor
    public Square (ImageView iv, int squareNo)
    {
        this.squareNo = squareNo;
        this.iv = iv;
        x = iv.getX() + 9 /*+ ((float) iv.getWidth())/4*/;
        y = iv.getY() + 9 /*+ ((float) iv.getHeight())/4*/;
    }

    // Getters/Setters
    public int getType()
    {
        return type;
    }

    public void setType(int type)
    {
        this.type = type;
    }

    public float getX()
    {
        return x;
    }

    public float getY()
    {
        return y;
    }

    public Square getCorrSquare()
    {
        return corrSquare;
    }

    public void setCorrSquare(Square corrSquare)
    {
        this.corrSquare = corrSquare;
    }

    public int getSquareNo()
    {
        return squareNo;
    }

    // Adds a Player to the Square
    public void addPlayers(Player player)
    {
        // Adds the Player to the square's ArrayList of Players
        players.add(player);

        // If there is already a player on the square, halve the size of each square and move one of them so they are both visible
        if (players.size() > 1)
        {
            for (int i = 0; i < players.size(); i++)
            {
                ImageView playerIV = players.get(i).getIv();
                ViewGroup.LayoutParams params = playerIV.getLayoutParams();
                params.width = params.width/2;
                params.height = params.height/2;
                playerIV.setLayoutParams(params);
            }

            float movement = Resources.getSystem().getDisplayMetrics().widthPixels/18f;

            players.get(1).getIv().setX(players.get(1).getIv().getX() + movement);
            players.get(1).getIv().setY(players.get(1).getIv().getY() + movement);
        }
    }

    public void removePlayers(Player player)
    {

        if (players.size() == 2)
        {
            ImageView playerIV = player.getIv();
            ViewGroup.LayoutParams params = playerIV.getLayoutParams();
            playerIV.setX(getX());
            playerIV.setY(getY());
            params.width = params.width * 2;
            params.height = params.height * 2;
            playerIV.setLayoutParams(params);
            players.remove(player);

            ImageView player2IV = players.get(0).getIv();
            ViewGroup.LayoutParams params2 = player2IV.getLayoutParams();
            params2.width = params2.width * 2;
            params2.height = params2.height * 2;
            player2IV.setLayoutParams(params2);
            player2IV.setX(getX());
            player2IV.setY(getY());
        }
        else
        {
            players.remove(player);
        }
    }

    // Gets the Players in the Square
    public ArrayList<Player> getPlayers()
    {
        return players;
    }

}
