package com.adarshhasija.flows;

import android.graphics.Bitmap;

import com.parse.ParseFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by adarshhasija on 5/18/15.
 */
public class Parse {

    public static File parseFileToJavaFile(ParseFile parseFile, String fileName, String fileFormat) {

        File file = null;

        try {
            file = File.createTempFile(fileName, fileFormat);
            byte[] bytes = parseFile.getData();

            FileOutputStream fileOuputStream =
                    new FileOutputStream(file);
            fileOuputStream.write(bytes);
            fileOuputStream.close();
        }catch(Exception e){
            e.printStackTrace();
        }

        return file;
    }


    public static ParseFile bitmapToParseFile(Bitmap bitmap, String filePrefix) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, stream);
        byte[] byteArray = stream.toByteArray();
        ParseFile parseFile = new ParseFile(filePrefix+".jpeg", byteArray);
        return parseFile;
    }
}
