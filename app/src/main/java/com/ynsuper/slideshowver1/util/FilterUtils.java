
package com.ynsuper.slideshowver1.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.ynsuper.slideshowver1.model.ListFilterModel;

import org.wysaid.common.SharedContext;
import org.wysaid.nativePort.CGEImageHandler;

import java.io.File;
import java.io.FileInputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class FilterUtils {

    public final static FilterBean OVERLAY_CONFIG[] = {
            new FilterBean("", ""),
            new FilterBean("#unpack @krblend sr overlay/01.jpg 100", ""),
            new FilterBean("#unpack @krblend sr overlay/2.jpg 100", ""),
            new FilterBean("#unpack @krblend sr overlay/02.jpg 100", ""),
            new FilterBean("#unpack @krblend sr overlay/3.jpg 100", ""),
            new FilterBean("#unpack @krblend sr overlay/4.jpg 100", ""),
            new FilterBean("#unpack @krblend sr overlay/5.png 100", ""),
            new FilterBean("#unpack @krblend sr overlay/1.jpg 100", ""),
            new FilterBean("#unpack @krblend sr overlay/19.jpg 100", ""),
            new FilterBean("#unpack @krblend sr overlay/46.jpg 100", ""),
            new FilterBean("#unpack @krblend sr overlay/47.jpg 100", ""),
            new FilterBean("#unpack @krblend sr overlay/effect_00005.jpg 100", ""),
            new FilterBean("#unpack @krblend sr overlay/26.jpg 100", ""),
            new FilterBean("#unpack @krblend sr overlay/35.jpg 100", ""),
            new FilterBean("#unpack @krblend sr overlay/42.jpg 100", ""),
            new FilterBean("#unpack @krblend sr overlay/43.jpg 100", ""),
            new FilterBean("#unpack @krblend sr overlay/44.jpg 100", ""),
            new FilterBean("#unpack @krblend sr overlay/45.jpg 100", ""),
            new FilterBean("#unpack @krblend sr overlay/effect_00018.jpg 100", ""),
            new FilterBean("#unpack @krblend sr overlay/effect_00025.jpg 100", ""),
            new FilterBean("#unpack @krblend sr overlay/effect_00026.jpg 100", ""),
            new FilterBean("#unpack @krblend sr overlay/effect_00031.jpg 100", ""),
            new FilterBean("#unpack @krblend sr overlay/effect_00037.jpg 100", ""),
            new FilterBean("#unpack @krblend sr overlay/53.jpg 100", ""),
            new FilterBean("#unpack @krblend sr overlay/54.jpg 100", ""),
            new FilterBean("#unpack @krblend sr overlay/55.jpg 100", ""),
            new FilterBean("#unpack @krblend sr overlay/56.jpg 100", ""),
            new FilterBean("#unpack @krblend sr overlay/57.jpg 100", ""),
            new FilterBean("#unpack @krblend sr overlay/11.png 100", ""),
            new FilterBean("#unpack @krblend sr overlay/12.png 100", ""),
            new FilterBean("#unpack @krblend sr overlay/13.png 100", ""),
    };

    public final static FilterBean EFFECT_CONFIGS[] = {
            new FilterBean("", "Original"),
            new FilterBean("@adjust lut filters/bright01.webp", "Fresh 01"),
            new FilterBean("@adjust lut filters/bright02.webp", "Fresh 02"),
            new FilterBean("@adjust lut filters/bright03.webp", "Fresh 03"),
            new FilterBean("@adjust lut filters/bright05.webp", "Fresh 05"),
//            new FilterBean("@adjust lut filters/portrait_film.png", "Portrait Film"),
//            new FilterBean("@adjust lut filters/cali_vibes.png", "Cali Vibes"),
//            new FilterBean("@adjust lut filters/arctic_chill.png", "Arctic Chill"),
//            new FilterBean("@adjust lut filters/vibrant_light.png", "Vibrant Light"),
//            new FilterBean("@adjust lut filters/binary_code.png", "Binary Code"),
//            new FilterBean("@adjust lut filters/soft_fade.png", "Soft Fade"),
//            new FilterBean("@adjust lut filters/lush_land.png", "Lush Land"),
//            new FilterBean("@adjust lut filters/hdr_color.png", "HDR Color"),
//            new FilterBean("@adjust lut filters/warm_teal.png", "Warm Teal"),
//            new FilterBean("@adjust lut filters/hollywood.png", "Hollywood"),
//            new FilterBean("@adjust lut filters/kodak_color.png", "Kodak Color"),
//            new FilterBean("@adjust lut filters/euro01.webp", "Euro 01"),
//            new FilterBean("@adjust lut filters/euro02.webp", "Euro 02"),
//            new FilterBean("@adjust lut filters/euro05.webp", "Euro 03"),
//            new FilterBean("@adjust lut filters/euro04.webp", "Euro 04"),
//            new FilterBean("@adjust lut filters/euro06.webp", "Euro 05"),
//            new FilterBean("@adjust lut filters/film01.webp", "Film 01"),
//            new FilterBean("@adjust lut filters/film02.webp", "Film 02"),
//            new FilterBean("@adjust lut filters/film03.webp", "Film 03"),
//            new FilterBean("@adjust lut filters/film04.webp", "Film 04"),
//            new FilterBean("@adjust lut filters/film05.webp", "Film 05"),
//            new FilterBean("@adjust lut filters/lomo1.webp", "Lomo 01"),
//            new FilterBean("@adjust lut filters/lomo2.webp", "Lomo 02"),
//            new FilterBean("@adjust lut filters/lomo3.webp", "Lomo 03"),
//            new FilterBean("@adjust lut filters/lomo4.webp", "Lomo 04"),
//            new FilterBean("@adjust lut filters/lomo5.webp", "Lomo 05"),
//            new FilterBean("@adjust lut filters/movie01.webp", "Movie 01"),
//            new FilterBean("@adjust lut filters/movie02.webp", "Movie 02"),
//            new FilterBean("@adjust lut filters/movie03.webp", "Movie 03"),
//            new FilterBean("@adjust lut filters/movie04.webp", "Movie 04"),
//            new FilterBean("@adjust lut filters/movie05.webp", "Movie 05"),
            new FilterBean("@adjust lut filters/cube1.jpg", "Cube 1"),
            new FilterBean("@adjust lut filters/cube2.jpg", "Cube 2"),
            new FilterBean("@adjust lut filters/cube3.jpg", "Cube 3"),
            new FilterBean("@adjust lut filters/cube4.jpg", "Cube 4"),
            new FilterBean("@adjust lut filters/cube5.jpg", "Cube 5"),
            new FilterBean("@adjust lut filters/cube6.jpg", "Cube 6"),
            new FilterBean("@adjust lut filters/cube7.jpg", "Cube 7"),
            new FilterBean("@adjust lut filters/cube8.jpg", "Cube 8"),
            new FilterBean("@adjust lut filters/cube9.jpg", "Cube 9"),
            new FilterBean("@adjust lut filters/cube10.jpg", "Cube 10"),
    };

    public static class FilterBean {
        private String config;
        private String name;

        FilterBean(String config, String name) {
            this.config = config;
            this.name = name;
        }

        public String getConfig() {
            return config;
        }

        public void setConfig(String config) {
            this.config = config;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static Bitmap getBlurImageFromBitmap(Bitmap bitmap) {
        SharedContext glContext = SharedContext.create();
        glContext.makeCurrent();
        CGEImageHandler handler = new CGEImageHandler();
        handler.initWithBitmap(bitmap);
        handler.setFilterWithConfig("@blur lerp 0.6");
        handler.processFilters();
        Bitmap bmp = handler.getResultBitmap();
        glContext.release();

        return bmp;
    }

    public static Bitmap getBlurImageFromBitmap(Bitmap bitmap, float intensity) {
        SharedContext glContext = SharedContext.create();
        glContext.makeCurrent();
        CGEImageHandler handler = new CGEImageHandler();
        handler.initWithBitmap(bitmap);
        handler.setFilterWithConfig(MessageFormat.format("@blur lerp {0}", intensity / 10.0f + ""));
        handler.processFilters();
        Bitmap bmp = handler.getResultBitmap();
        glContext.release();

        return bmp;
    }

    public static Bitmap cloneBitmap(Bitmap bitmap) {
        SharedContext glContext = SharedContext.create();
        glContext.makeCurrent();
        CGEImageHandler handler = new CGEImageHandler();
        handler.initWithBitmap(bitmap);
        handler.setFilterWithConfig("");
        handler.processFilters();
        Bitmap bmp = handler.getResultBitmap();
        glContext.release();

        return bmp;
    }

    public static Bitmap getBlackAndWhiteImageFromBitmap(Bitmap bitmap) {
        SharedContext glContext = SharedContext.create();
        glContext.makeCurrent();
        CGEImageHandler handler = new CGEImageHandler();
        handler.initWithBitmap(bitmap);
        handler.setFilterWithConfig("@adjust saturation 0");
        handler.processFilters();
        Bitmap bmp = handler.getResultBitmap();
        glContext.release();

        return bmp;
    }

    public static Bitmap getBitmapWithFilter(Bitmap bitmap, String config) {

        SharedContext glContext = SharedContext.create();
        glContext.makeCurrent();
        CGEImageHandler handler = new CGEImageHandler();
        handler.initWithBitmap(bitmap);
        handler.setFilterWithConfig(config);
        handler.setFilterIntensity(0.8f);
        handler.processFilters();
        Bitmap bmp = handler.getResultBitmap();
        glContext.release();

        return bmp;
    }

    public static List<Bitmap> getLstBitmapWithFilter(Bitmap bitmap, ArrayList<ListFilterModel> listFilterModel) {

        List<Bitmap> lstBitmaps = new ArrayList<>();

        SharedContext glContext = SharedContext.create();
        glContext.makeCurrent();
        CGEImageHandler handler = new CGEImageHandler();
        handler.initWithBitmap(bitmap);

        if (listFilterModel != null) {
            for (ListFilterModel config : listFilterModel) {
                if (config.isFile()) {
                    handler.setFilterWithConfig("@adjust lut " + Constants.PATH_DOWNLOAD_FILTER_FROM_CLOUD.substring(1) + config.getTextFilter());
                } else {
                    handler.setFilterWithConfig(config.getTextFilter());
                }
                handler.processFilters();
                Bitmap bmp = handler.getResultBitmap();
                lstBitmaps.add(bmp);
            }
        } else {
            for (FilterBean config : EFFECT_CONFIGS) {
                handler.setFilterWithConfig(config.getConfig());
                handler.processFilters();
                Bitmap bmp = handler.getResultBitmap();
                lstBitmaps.add(bmp);
            }
        }
        glContext.release();

        return lstBitmaps;
    }

    public static List<Bitmap> getLstBitmapWithOverlay(Bitmap bitmap) {

        List<Bitmap> lstBitmaps = new ArrayList<>();

        SharedContext glContext = SharedContext.create();
        glContext.makeCurrent();
        CGEImageHandler handler = new CGEImageHandler();
        handler.initWithBitmap(bitmap);
        for (FilterBean config : OVERLAY_CONFIG) {
            handler.setFilterWithConfig(config.getConfig());
            handler.processFilters();
            Bitmap bmp = handler.getResultBitmap();
            lstBitmaps.add(bmp);
        }
        glContext.release();

        return lstBitmaps;
    }

    public static Bitmap getLstBitmapWithStringOverlay(Bitmap bitmap, String config) {


        SharedContext glContext = SharedContext.create();
        glContext.makeCurrent();
        CGEImageHandler handler = new CGEImageHandler();
        handler.initWithBitmap(bitmap);
        handler.setFilterWithConfig(config);
        handler.processFilters();
        Bitmap bmp = handler.getResultBitmap();

        glContext.release();

        return bmp;
    }


    public static Bitmap getBitmapFromPath(String path) {
        Bitmap bitmap = null;
        try {
            File f = new File(path);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            bitmap = BitmapFactory.decodeStream(new FileInputStream(f), null, options);
        } catch (Exception e) {
            Log.d("Ynsuper", "Exception: " + e.getLocalizedMessage());
            e.printStackTrace();
        }
        return bitmap;
    }

}
