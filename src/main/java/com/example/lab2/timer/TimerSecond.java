package com.example.lab2.timer;

import javafx.animation.AnimationTimer;

public class TimerSecond extends AnimationTimer {
    private long lastTime = 0;
    public int seconds;
    Integer t;

    private Updatable updatables[];

    public TimerSecond ( Updatable ...updatables ) {
        this.updatables = new Updatable[updatables.length];
        for ( int i = 0; i < updatables.length; ++i ) {
            this.updatables[i] = updatables[i];
        }
    }


    @Override
    public void handle(long now) {
        if (lastTime != 0) {
            if (now > lastTime + 1_000_000_000) {
                seconds++;
                //System.out.println(seconds);
                lastTime = now;
            }
        } else {
            lastTime = now;

        }
    }
}
