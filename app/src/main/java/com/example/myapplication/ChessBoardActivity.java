package com.example.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ChessBoardActivity extends View implements View.OnTouchListener{
    //变量
    private final static int row_col = 15;//棋盘的行列数
    private final static int NO_CHESS = 0;//表示对应位置没有棋子：
    private final static int WHITE_CHESS = 1;//表示对应位置有白棋
    private final static int BLACK_CHESS = 2;//表示对应位置有黑棋
    private ChessActivity[] chess_store;//保存棋盘上棋子的信息（分配内存为225个）
    private int[][] position_record;//记录棋盘对应位置是否有棋子（没有棋子时全部为0，落黑子就表为2，落白子就标为1）
    private int chess_count;//记录当前棋子数，悔棋的时候通过棋子数去获取当前棋子的位置标志位置的数据为0
    private boolean is_win;//表示输赢
    private boolean is_black;//表示当前棋手
    private boolean is_player;//表示是否是玩家落子
    private float line_space;//棋盘的间隔
    private float length;//棋盘的长度
    private final Paint paint;//画笔
    Bitmap white_chess;//保存白棋图片
    Bitmap black_chess;//保存黑棋图片
    Rect rect;//位置
    GameCallBack callBack;//回调接口
    Point ai_point;//AI落子点
    Point start_point;//判断位置
    Point end_point;//判断位置



    //绘制棋盘
    public ChessBoardActivity(Context context, Paint paint) {
        super(context);
        //画笔
        this.paint = paint;
    }
    public ChessBoardActivity(Context context) {
        this(context, (Paint) null);
    }
    public ChessBoardActivity(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChessBoardActivity(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        white_chess = BitmapFactory.decodeResource(context.getResources(), R.drawable.white);
        black_chess = BitmapFactory.decodeResource(context.getResources(), R.drawable.black);
        //初始化Paint
        paint = new Paint();
        //设置抗锯齿
        paint.setAntiAlias(true);
        paint.setColor(Color.BLUE);
        rect = new Rect();
        init();
        setOnTouchListener(this);
    }
    //用来计算手机所使用的高度和宽度
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //获取高宽值
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        //获取宽高中较小的值
        int length = Math.min(width, height);
        //重新设置宽高
        setMeasuredDimension(length, length);
    }
    //初始化一些变量的数据
    public void init(){
        chess_store = new ChessActivity[row_col * row_col];
        for(int i = 0;i < row_col * row_col;i++){
            chess_store[i] = new ChessActivity();
        }

        position_record = new int[row_col][row_col];
        for(int i = 0;i < row_col;i++){
            for(int j = 0;j < row_col;j++){
                position_record[i][j] = 0;
            }
        }
        chess_count = 0;
        is_win = false;
        is_black = true;
        is_player = true;
        ai_point = new Point();
        start_point = new Point();
        end_point = new Point();
    }
    //绘制棋盘和棋子
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //棋盘的高度和宽度
        length = Math.min(getWidth(), getHeight());
        line_space = length / row_col;
        // 设置画笔线宽
        paint.setStrokeWidth(5);
        //绘制棋盘
        for (int i = 0; i < row_col; i++) {
            float start = i * line_space + line_space / 2;
            //横线
            canvas.drawLine(line_space / 2, start, length - line_space / 2, start, paint);
            //竖线
            canvas.drawLine(start, line_space / 2, start, length - line_space / 2, paint);
        }
        //绘制棋子
        for (int i = 0; i < row_col; i++) {
            for (int j = 0; j < row_col; j++) {
                //rect中点坐标
                float rectX = line_space / 2 + i * line_space;
                float rectY = line_space / 2 + j * line_space;
                //设置rect位置
                rect.set((int) (rectX - line_space / 2), (int) (rectY - line_space / 2),
                        (int) (rectX + line_space / 2), (int) (rectY + line_space / 2));
                if(position_record[i][j] == WHITE_CHESS){
                    canvas.drawBitmap(white_chess, null, rect, paint);
                }
                else if(position_record[i][j] == BLACK_CHESS){
                    canvas.drawBitmap(black_chess, null, rect, paint);
                }
            }
        }
    }
    //屏幕点击或有其他动作时的响应函数，即点击落子
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            //获取按下时的位置
            float downX = event.getX();
            float downY = event.getY();
            //点击的位置在棋盘上
            if (downX >= line_space / 4 && downX <= length - line_space / 4
                    && downY >= line_space / 4 && downY <= length - line_space / 4) {
                //获取棋子对应的准确位置
                int x = (int) (downX / line_space);
                int y = (int) (downY / line_space);
                addChess(x,y);

                if (callBack.getMode().equals("人机对战") && !is_player) {
                    if(getLocation(is_black ? BLACK_CHESS : WHITE_CHESS)){
                        addChess(ai_point.x,ai_point.y);
                    }
                    else{
                        callBack.callBackInfo("和棋");
                    }
                }
            }
        }
        return false;
    }
    //获取Ai要落子位置，位置用于九宫格落子位置查询
    public void setAiPoint(){
        if(chess_count == 0){
            ai_point.set(row_col / 2,row_col / 2);
        }
        else if(chess_count == 1){
            ai_point.set(chess_store[0].getX(),chess_store[0].getY());
        }
        else {
            int x = chess_store[chess_count - 2].getX();
            int y = chess_store[chess_count - 2].getY();
            ai_point.set(x,y);
        }
    }
    //AI获取落子位函数
    public boolean getLocation(int color){
        int on_color = color == WHITE_CHESS ? BLACK_CHESS : WHITE_CHESS;
        setAiPoint();

        if(intercept(4,color)){
            return true;//检测自己有四子相连
        }
        else if(intercept(4,on_color)){
            return true;//检测对手有四子相连
        }
        else if(intercept(3,color)){
            return true;//检测自己有三子相连
        }
        else if(intercept(3,on_color)){
            return true;//检测对手有三子相连
        }
        else if(breakTriangle(color)){
            return true;
        }
        else if(breakTriangle(on_color)){
            return true;
        }
        else if(intercept(2, color)) {
            return true;
        }
        else return jiuGongGe(ai_point.x,ai_point.y,on_color);

    }
    //落子时向一些保存信息的变量添加数据
    public void addChess(int x, int y) {
        // 判断当前位置是否已经有子
        if (position_record[x][y] == NO_CHESS && !is_win) {
            // 给数组赋值
            position_record[x][y] = is_black ? BLACK_CHESS : WHITE_CHESS;
            chess_store[chess_count].setInfo(x, y, position_record[x][y]);
            chess_count++;
            // 修改当前执子
            is_black = !is_black;
            // 更新棋盘
            postInvalidate();
            // 判断游戏是否结束
            if (gameOver(x, y, position_record[x][y])) {
                is_win = true;
                // 游戏结束时弹出对话框
                if (!is_black) {
                    callBack.callBackInfo("黑棋赢了！");
                } else {
                    callBack.callBackInfo("白棋赢了！");
                }
            }
            // 回调当前执子
            if (callBack != null) {
                callBack.chessBoardChange(x, y);
            }
            is_player = !is_player;
        }
    }

    //悔棋
    public void subChess(){
        if(chess_count > 0){
            chess_count--;
            int x = chess_store[chess_count].getX();
            int y = chess_store[chess_count].getY();
            position_record[x][y] = NO_CHESS;
            postInvalidate();
            is_win = false;
            is_black = !is_black;
        }
        else{
            callBack.callBackInfo("无棋可悔");
        }
    }
    //设置回调
    public void setCallBack(GameCallBack callBack) {
        this.callBack = callBack;
    }
    //判断是否胜利
    public boolean gameOver(int x, int y, int color){
        return intercept_SN(x, y, 4, color) || intercept_EW(x,y,4,color)
                || intercept_WN_ES(x,y,4,color) || intercept_EN_WS(x,y,4,color);
    }
    //判断是否可以悔棋西北到东南方向的拦截，即从西北到东南方向上是否有符合某些条件的棋局。判断是否可以悔棋指南北方向的拦截，即从南到北方向上是否有符合某些条件的棋局。
    public boolean intercept(int max, int color){
        for(int x = 0;x < row_col;x++){
            for(int y = 0;y < row_col;y++){
                if(position_record[x][y] == NO_CHESS){
                    if((intercept_SN(x,y,max,color) || intercept_EW(x,y,max,color) ||
                            intercept_WN_ES(x,y,max,color) || intercept_EN_WS(x,y,max,color)) &&
                            interceptJudgement(max)) {
                        ai_point.set(x,y);
                        return true;
                    }
                }
            }
        }
        return false;
    }
    //
    public boolean interceptJudgement(int max){
        int x1 = start_point.x;
        int y1 = start_point.y;

        int x2 = end_point.x;
        int y2 = end_point.y;

        switch(max){
            case 4:
                return true;
            case 3:
                return position_record[x1][y1] == NO_CHESS ||
                        position_record[x2][y2] == NO_CHESS;
            case 2:
                return position_record[x1][y1] == NO_CHESS &&
                        position_record[x2][y2] == NO_CHESS;
        }
        return false;
    }

    public boolean intercept_SN(int x, int y, int max, int color){
        int yt;
        int count = 0;
        yt = y;
        while(yt - 1 >= 0 && position_record[x][yt - 1] == color){
            count++;yt--;
        }
        yt = yt - 1 >= 0 ? yt - 1 : yt;
        start_point.set(x,yt);
        yt = y;
        while(yt + 1 < 15 && position_record[x][yt + 1] == color){
            count++;yt++;
        }
        yt = yt + 1 < 15 ? yt + 1 : yt;
        end_point.set(x,yt);
        return count >= max;
    }

    public boolean intercept_EW(int x, int y, int max, int color){
        int xt;
        int count = 0;
        xt = x;
        while(xt - 1 >= 0 && position_record[xt - 1][y] == color){
            count++;xt--;
        }
        xt = xt - 1 >= 0 ? xt - 1: xt;
        start_point.set(xt,y);
        xt = x;
        while(xt + 1 < 15 && position_record[xt + 1][y] == color){
            count++;xt++;
        }
        xt = xt + 1 < 15 ? xt + 1: xt;
        end_point.set(xt,y);
        return count >= max;
    }

    public boolean intercept_WN_ES(int x, int y, int max, int color){
        int xt,yt;
        int count = 0;
        xt = x;yt = y;
        while(xt - 1 >= 0 && yt - 1 >= 0 && position_record[xt - 1][yt - 1] == color){
            count++;xt--;yt--;
        }
        xt = xt - 1 >= 0 ? xt - 1 : xt;
        yt = yt - 1 >= 0 ? yt - 1 : yt;
        start_point.set(xt,yt);
        xt = x;yt = y;
        while(xt + 1 < 15 && yt + 1 < 15 && position_record[xt + 1][yt + 1] == color){
            count++;xt++;yt++;
        }
        xt = xt + 1 < 15 ? xt + 1 : xt;
        yt = yt + 1 < 15 ? yt + 1 : yt;
        end_point.set(xt,yt);
        return count >= max;
    }

    public boolean intercept_EN_WS(int x, int y, int max, int color){
        int xt,yt;
        int count = 0;
        xt = x;yt = y;
        while(xt + 1 < 15 && yt - 1 >= 0 && position_record[xt + 1][yt - 1] == color){
            count++;xt++;yt--;
        }
        xt = xt + 1 < 15 ? xt + 1 : xt;
        yt = yt - 1 >= 0 ? yt - 1 : yt;
        start_point.set(xt,yt);
        xt = x;
        yt = y;
        while(xt - 1 >= 0 && yt + 1 < 15 && position_record[xt - 1][yt + 1] == color){
            count++;
            xt--;yt++;
        }
        xt = xt - 1 >= 0 ? xt - 1 : xt;
        yt = yt + 1 < 15 ? yt + 1 : yt;
        end_point.set(xt,yt);
        return count >= max;
    }
    //判断是否存在九宫格内可以下棋的位置。如果某个位置在九宫格内为空且周围存在对手的棋子，则可以下在该位置
    public boolean jiuGongGe(int x,int y,int on_color){
        //南北
        if(y - 1 >= 0 && position_record[x][y - 1] == NO_CHESS &&
                y + 1 < 15 && position_record[x][y + 1] != on_color){
            ai_point.set(x,y-1);
            return true;
        }
        if(y + 1 < 15 && position_record[x][y + 1] == NO_CHESS &&
                y - 1 >= 0 && position_record[x][y - 1] != on_color){
            ai_point.set(x,y+1);
            return true;
        }
        //东西
        if(x - 1 >= 0 && position_record[x - 1][y] == NO_CHESS &&
                x + 1 < 15 && position_record[x + 1][y] != on_color){
            ai_point.set(x - 1,y);
            return true;
        }
        if(x + 1 < 15 && position_record[x + 1][y] == NO_CHESS &&
                x - 1 >= 0 && position_record[x - 1][y] != on_color){
            ai_point.set(x+1,y);
            return true;
        }
        //西北 东南
        if(x - 1 >= 0 && y - 1 >= 0 && position_record[x - 1][y - 1] == NO_CHESS &&
                x + 1 < 15 && y + 1 < 15 && position_record[x + 1][y + 1] != on_color){
            ai_point.set(x - 1,y - 1);
            return true;
        }
        if(x + 1 < 15 && y + 1 < 15 && position_record[x + 1][y + 1] == NO_CHESS &&
                x - 1 >= 0 && y - 1 >= 0 && position_record[x - 1][y - 1] != on_color){
            ai_point.set(x+1,y + 1);
            return true;
        }
        //东北 西南
        if(x + 1 < 15 && y - 1 >= 0 && position_record[x + 1][y - 1] == NO_CHESS &&
                x - 1 >= 0 && y + 1 < 15 && position_record[x - 1][y + 1] != on_color){
            ai_point.set(x + 1,y - 1);
            return true;
        }
        if(x - 1 >= 0 && y + 1 < 15 && position_record[x - 1][y + 1] == NO_CHESS &&
                x + 1 < 15 && y - 1 >= 0 && position_record[x + 1][y - 1] != on_color){
            ai_point.set(x-1,y + 1);
            return true;
        }
        return false;
    }
    //检测是否存在破坏对方三连的情况
    public boolean breakTriangle(int color){
        int count;
        for(int x = 0;x < row_col;x++){
            for(int y = 0;y < row_col;y++){
                if(position_record[x][y] == NO_CHESS){
                    count = 0;
                    count += breakTriangle_SN(x,y,color);
                    count += breakTriangle_EW(x,y,color);
                    count += breakTriangle_WN_WS(x,y,color);
                    count += breakTriangle_EN_ES(x,y,color);

                    if(count >= 2){
                        ai_point.set(x,y);
                        return true;
                    }
                }
            }
        }
        return false;
    }
    public int breakTriangle_SN(int x, int y, int color)
    {
        //南方
        int count = 0;
        int xi = x;
        int yi = y;
        int k = 1;
        if ((yi - 1) >= 0 && position_record[xi][yi - 1] == NO_CHESS) {
            k++;
        }
        yi++;
        while (yi >= 0 && yi < row_col && position_record[xi][yi] == color) {
            k++;yi++;
        }
        if (yi < row_col && position_record[xi][yi] == NO_CHESS) {
            k++;
        }
        if (k >= 5) {
            count++;
        }
        //北方
        xi = x;yi = y;k = 1;
        if ((yi + 1) < row_col && position_record[xi][yi + 1] == NO_CHESS) {
            k++;
        }
        yi--;
        while (yi >= 0 && yi < row_col && position_record[xi][yi] == color) {
            k++;yi--;
        }
        if (yi >= 0 && position_record[xi][yi] == NO_CHESS) {
            k++;
        }
        if (k >= 5) {
            count++;
        }
        return count;
    }
    public int breakTriangle_EW(int x, int y, int color)
    {
        //东方
        int count = 0;
        int xi = x, yi = y;
        int k = 1;
        if ((xi - 1) >= 0 && position_record[xi - 1][yi] == NO_CHESS) {
            k++;
        }
        xi++;
        while (xi < row_col && position_record[xi][yi] == color) {
            k++;xi++;
        }
        if (xi < row_col && position_record[xi][yi] == NO_CHESS) {
            k++;
        }
        if (k >= 5) {
            count++;
        }

        //西方
        xi = x;yi = y;k = 1;
        if ((xi + 1) < row_col && position_record[xi + 1][yi] == NO_CHESS) {
            k++;
        }
        xi--;
        while (xi >= 0 && xi < row_col && position_record[xi][yi] == color) {
            k++;xi--;
        }
        if (xi > 0 && position_record[xi][yi] == NO_CHESS) {
            k++;
        }
        if (k >= 5) {
            count++;
        }
        return count;
    }
    public int breakTriangle_WN_WS(int x, int y, int color)
    {
        //西北
        int count = 0;
        int xi = x, yi = y;
        int k = 1;
        if ((xi + 1) < row_col && (yi + 1) < row_col && position_record[xi + 1][yi + 1] == NO_CHESS) {
            k++;
        }
        xi--;yi--;
        while (yi >= 0 && yi < row_col && xi >= 0 && xi < row_col && position_record[xi][yi] == color) {
            k++;xi--;yi--;
        }
        if (xi >= 0 && yi >= 0 && position_record[xi][yi] == NO_CHESS) {
            k++;
        }
        if (k >= 5) {
            count++;
        }
        //西南
        xi = x;yi = y;
        k = 1;
        if ((xi + 1) < row_col && (yi - 1) >= 0 && position_record[xi + 1][yi - 1] == NO_CHESS) {
            k++;
        }
        xi--;yi++;
        while (yi >= 0 && yi < row_col && xi >= 0 && xi < row_col && position_record[xi][yi] == color) {
            k++;xi--;yi++;
        }
        if (xi >= 0 && yi < row_col && position_record[xi][yi] == NO_CHESS) {
            k++;
        }
        if (k >= 5) {
            count++;
        }
        return count;
    }
    public int breakTriangle_EN_ES(int x, int y, int color)
    {
        //东南
        int count = 0;
        int xi = x, yi = y;
        int k = 1;
        if ((xi - 1) >= 0 && (yi - 1) >= 0 && position_record[xi - 1][yi - 1] == NO_CHESS) {
            k++;
        }
        xi++;yi++;
        while (yi >= 0 && yi < row_col && xi >= 0 && xi < row_col && position_record[xi][yi] == color) {
            k++;xi++;yi++;
        }
        if (xi < row_col && yi < row_col && position_record[xi][yi] == 0) {
            k++;
        }
        if (k >= 5) {
            count++;
        }
        //东北
        xi = x;yi = y;
        k = 1;
        if ((xi - 1) >= 0 && (yi + 1) < row_col && position_record[xi - 1][yi + 1] == NO_CHESS) {
            k++;
        }
        xi++;yi--;
        while (yi >= 0 && yi < row_col && xi >= 0 && xi < row_col && position_record[xi][yi] == color) {
            k++;xi++;yi--;
        }
        if (xi < row_col && yi >= 0 && position_record[xi][yi] == NO_CHESS) {
            k++;
        }
        if (k >= 5) {
            count++;
        }
        return count;
    }
}
