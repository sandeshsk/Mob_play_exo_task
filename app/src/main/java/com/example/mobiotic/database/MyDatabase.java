package com.example.mobiotic.database;

import android.content.Context;

import com.example.mobiotic.util.AppUtil;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;


@Database(entities = MediaDetail.class, version = 1)
public abstract class MyDatabase extends RoomDatabase {

    public abstract MediaDetailDao getMediaDetailDao();

    private static MyDatabase INSTANCE;

    public static MyDatabase getAppDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE =
                    Room.databaseBuilder(context.getApplicationContext(), MyDatabase.class, AppUtil.DATABASE_NAME)
                            // allow queries on the main thread.
                            .allowMainThreadQueries()
                            .fallbackToDestructiveMigration()
                            .build();
        }
        return INSTANCE;
    }

}
