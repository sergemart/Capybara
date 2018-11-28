package com.github.sergemart.mobile.capybara.data.dao;

import com.github.sergemart.mobile.capybara.data.model.FamilyMember;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import io.reactivex.Completable;
import io.reactivex.Flowable;


@Dao
public interface FamilyMemberDao {

    @Query("SELECT * FROM family_member")
    Flowable<List<FamilyMember>> readAll();


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable createOrUpdate(FamilyMember ... familyMembers);


    @Query("DELETE FROM family_member")
    void deleteAll();                                                                               // unable to return an observable; a bug? TODO: check when Room release is published


}
