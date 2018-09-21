package com.example.mobiotic.database;


import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface MediaDetailDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMediaDetails(List<MediaDetail> mediaModels);

    @Query("SELECT * FROM MediaDetail where id=:id")
    MediaDetail chooseMediaDetailById(String id);

    @Query("Select * from mediadetail ORDER BY title ASC")
    List<MediaDetail> chooseAllMediaDetails();

    @Query("Select * from mediadetail ORDER By (CASE WHEN id =:id THEN 0 ELSE 1 END), id ASC")
    List<MediaDetail> getMediaWithTopSelected(String id);

    @Query("Select lastPlayedPostion from mediadetail where id=:id")
    long getDurationOfPerticularMedia(String id);


}
