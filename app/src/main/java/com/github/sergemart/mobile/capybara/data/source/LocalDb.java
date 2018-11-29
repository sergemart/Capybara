package com.github.sergemart.mobile.capybara.data.source;

import com.github.sergemart.mobile.capybara.App;
import com.github.sergemart.mobile.capybara.Constants;
import com.github.sergemart.mobile.capybara.data.dao.FamilyMemberDao;
import com.github.sergemart.mobile.capybara.data.model.FamilyMember;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;


// Singleton
@Database(entities = {FamilyMember.class}, version = 1, exportSchema = false)
public abstract class LocalDb
    extends RoomDatabase
{

    private static volatile LocalDb sInstance = null;



    // Factory method
    public static LocalDb get() {
        if(sInstance == null) {
//            sInstance = Room.inMemoryDatabaseBuilder(App.getContext(), LocalDb.class).build();
            sInstance = Room.databaseBuilder(App.getContext(), LocalDb.class, Constants.DB_NAME).build();
        }
        return sInstance;
    }


    // --------------------------- DAO getters

    abstract FamilyMemberDao getFamilyMemberDao();


}
