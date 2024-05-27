package com.example.myapplication;
//棋子类主要用于表示棋子，便于我们获取棋子的位置和棋子的颜色等信息。
public class ChessActivity {
    private int x;
    private int y;
    private int color;

    ChessActivity() {
        this.x = 0;
        this.y = 0;
        this.color = 0;
    }

    ChessActivity(ChessActivity chessActivity) {
        this.x = chessActivity.x;
        this.y = chessActivity.y;
        this.color = chessActivity.color;
    }

    public void setInfo(int x, int y, int color){
        this.x = x;
        this.y = y;
        this.color = color;
    }

    public String getInfo(){
        return String.valueOf(x) + " " + String.valueOf(y) + " " + String.valueOf(color);
    }

    public int getX() {
        return this.x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return this.y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getColor() {
        return this.color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
