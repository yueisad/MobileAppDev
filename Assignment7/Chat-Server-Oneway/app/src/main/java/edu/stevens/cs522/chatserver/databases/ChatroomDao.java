package edu.stevens.cs522.chatserver.databases;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import edu.stevens.cs522.chatserver.entities.Chatroom;

/*
 * Make sure to declare an index on chatroom name, that specifies chat names are unique.
 */
@Dao
public abstract class ChatroomDao {

    @Query("SELECT * FROM  Chatroom")
    public abstract LiveData<List<Chatroom>> fetchAllChatrooms();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract void insert(Chatroom chatroom);

}
