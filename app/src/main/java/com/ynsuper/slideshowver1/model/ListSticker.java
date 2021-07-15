package com.ynsuper.slideshowver1.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class ListSticker {
    @SerializedName("sticker")
     ArrayList<StickerModel> arraySticker;

    public ListSticker() {
    }

    public ListSticker(ArrayList<StickerModel> arraySticker) {
        this.arraySticker = arraySticker;
    }

    public ArrayList<StickerModel> getArraySticker() {
        if (arraySticker==null){
            arraySticker = new ArrayList<>();
        }
        return arraySticker;
    }

    public void setArraySticker(ArrayList<StickerModel> arraySticker) {
        this.arraySticker = arraySticker;
    }
}
