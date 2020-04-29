package com.macindex.macindex;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

class CategoryHelper {

    private static final Map<Integer, Integer> getLayout;
    static {
        getLayout = new HashMap<>();
        // Put more Categories after update.
        getLayout.put(0, R.id.category0Layout);
        getLayout.put(1, R.id.category1Layout);
        getLayout.put(2, R.id.category2Layout);
        getLayout.put(3, R.id.category3Layout);
        getLayout.put(4, R.id.category4Layout);
        getLayout.put(5, R.id.category5Layout);
        getLayout.put(6, R.id.category6Layout);
        getLayout.put(7, R.id.category7Layout);
        getLayout.put(8, R.id.category8Layout);
        getLayout.put(9, R.id.category9Layout);
    }
    static int getLayout(final int category) {
        try {
            return getLayout.get(category);
        } catch (Exception e) {
            Log.e("CategoryLayout","Failed with " + category);
            e.printStackTrace();
        }
        return 0;
    }
}
