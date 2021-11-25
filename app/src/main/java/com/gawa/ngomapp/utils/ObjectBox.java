package com.gawa.ngomapp.utils;

import com.gawa.ngomapp.models.MyObjectBox;

import io.objectbox.BoxStore;

public class ObjectBox {
    
    private static BoxStore boxStore;
    
    
    static void init(App context){
        boxStore = MyObjectBox.builder()
                .androidContext(context.getApplicationContext())
                .build();
    }

    public static BoxStore get(){
        return boxStore;
    }
}
