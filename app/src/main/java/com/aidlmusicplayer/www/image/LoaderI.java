package com.aidlmusicplayer.www.image;

import android.content.Context;

/**
 * Created by zjl on 16-9-27.
 */

public interface LoaderI {

    void load(Context context, ImageLoader var1);

    void animate(Context context, ImageLoader var2);

    void transform(Context context, ImageLoader var3);
}
