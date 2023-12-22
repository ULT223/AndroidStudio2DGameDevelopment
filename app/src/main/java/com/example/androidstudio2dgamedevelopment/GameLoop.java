package com.example.androidstudio2dgamedevelopment;

import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;


/**
 * Game manages all objects in the game and responsible for updating all states and render all
 * object to the screen
 */
public class GameLoop extends Thread{
    private static final double MAX_UPS = 60.0;
    private static final double UPS_PERIOD = 1E+3 / MAX_UPS;
    private Game game;
    private boolean isRunning = false;
    private SurfaceHolder surfaceHolder;
    private double averageUPS;
    private double averageFPS;


    public GameLoop(Game game, SurfaceHolder surfaceHolder) {
        this.surfaceHolder = surfaceHolder;
        this.game = game;
    }

    public double getAverageUPS() {
        return  averageUPS;
    }

    public double getAverageFPS() {
        return  averageFPS;
    }

    public void startLoop() {
        isRunning = true;
        this.start();
    }

    @Override
    public void run() {
        super.run();

        //Declare time and cycle count variables
        int updateCount = 0;
        int frameCount = 0;

        long startTime;
        long elaspedTime;
        long sleepTime;

        Log.d("Game", "Game Loop Run");
        Canvas canvas = null;
        startTime = System.currentTimeMillis();
        //GameLoop
        while(isRunning){
            // Try to update and render game
            try {
                canvas = surfaceHolder.lockCanvas();
                synchronized (surfaceHolder){
                    game.update();
                    updateCount++;
                    game.draw(canvas);
                }
            }catch (IllegalArgumentException e){
                e.printStackTrace();
            }finally {
                if (canvas != null){
                    try{
                        surfaceHolder.unlockCanvasAndPost(canvas);
                        frameCount++;
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }




            //Pause game loop to not exceed target UPS
            elaspedTime = System.currentTimeMillis() - startTime;
            sleepTime = (long)(updateCount * UPS_PERIOD - elaspedTime);
            if(sleepTime > 0){
                try {
                    sleep(sleepTime);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            //Skip frames to keep up with targer UPS
            while(sleepTime < 0 && updateCount < MAX_UPS - 1){
                game.update();
                updateCount++;
                elaspedTime = System.currentTimeMillis() - startTime;
                sleepTime = (long)(updateCount * UPS_PERIOD - elaspedTime);
            }
            //Calculate average UPS and FPS
            elaspedTime = System.currentTimeMillis() - startTime;
            if (elaspedTime >= 1000){
                averageUPS = updateCount / (1E-3 * elaspedTime);
                averageFPS = updateCount / (1E-3 * elaspedTime);
                updateCount = 0;
                frameCount = 0;
                startTime = System.currentTimeMillis();
            }
        }
    }


}
