package com.example.myapplication;
//回调接口
public interface GameCallBack {
    void callBackInfo(String info);
    void chessBoardChange(int current_x, int current_y);
    String getMode();
}
