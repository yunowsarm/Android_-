package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

// 主活动类，实现了 GameCallBack 接口
public class MainActivity extends AppCompatActivity implements GameCallBack {
    ChessBoardActivity chess_board; // 棋盘活动

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chess_board = findViewById(R.id.view_five_chess_board); // 获取棋盘视图
        chess_board.setCallBack(this); // 设置回调接口

        init(); // 初始化方法
    }

    // 初始化方法，设置按钮的点击事件监听器
    public  void init() {
        Button btn_begin = findViewById(R.id.btn_begin); // 开始按钮
        Button btn_prompt = findViewById(R.id.btn_prompt); // 提示按钮
        Button btn_repent = findViewById(R.id.btn_repent); // 悔棋按钮
        Button btn_help = findViewById(R.id.btn_help); // 帮助按钮

        // 开始按钮点击事件
        btn_begin.setOnClickListener(view -> {
            chess_board.init(); // 初始化棋盘
            chess_board.postInvalidate(); // 刷新棋盘视图
        });

        // 提示按钮点击事件
        btn_prompt.setOnClickListener(view-> new AlertDialog.Builder(MainActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("提示")
                .setMessage("看看有没有三子或四子相连的地方喔！")
                .setPositiveButton("确定", (dialogInterface, i) -> {

                }).show());

        // 悔棋按钮点击事件
        btn_repent.setOnClickListener(view -> chess_board.subChess());

        // 帮助按钮点击事件
        btn_help.setOnClickListener(view -> new AlertDialog.Builder(MainActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("帮助")
                .setMessage("点击开始可以刷新棋盘重新开始。")
                .setPositiveButton("确定", (dialogInterface, i) -> {

                }).show());
    }

    // 回调接口方法，用于显示信息提示
    @Override
    public void callBackInfo(String info) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("游戏结束")
                    .setMessage(info)
                    .setPositiveButton("确定", null) // 添加确定按钮，点击后关闭对话框
                    .show(); // 显示对话框
    }

    // 回调接口方法，用于响应落子事件
    @Override
    public void chessBoardChange(int current_x, int current_y) {
        return;
    }

    // 获取游戏模式
    @Override
    public String getMode(){
        Spinner spi_mode = findViewById(R.id.spinner_mode); // 获取模式选择器
        return spi_mode.getSelectedItem().toString(); // 返回选择的模式
    }
}
