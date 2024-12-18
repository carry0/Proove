package com.yscoco.lib.util;

import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.CycleInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;

public class AnimUtil {
    public static void startRotateAnimation(View view, float rps) {
        if (view == null) {
            return;
        }
        long animateTime = (long) (1000L / rps);
        RotateAnimation animation = new RotateAnimation(0f, 360f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setFillAfter(true);
        animation.setRepeatCount(Animation.INFINITE);
        animation.setInterpolator(new LinearInterpolator());
        animation.setDuration(animateTime);
        view.startAnimation(animation);
    }

    public static void stopAnimation(View view) {
        if (view == null || view.getAnimation() == null) {
            return;
        }
        view.getAnimation().cancel();
    }

    public static void jitterView(View view) {
        ObjectAnimator oa1 = ObjectAnimator.ofFloat(view, View.TRANSLATION_X.getName(), 0, 8f);
        oa1.setDuration(300);
        oa1.setInterpolator(new CycleInterpolator(4));
        oa1.start();
    }
}
