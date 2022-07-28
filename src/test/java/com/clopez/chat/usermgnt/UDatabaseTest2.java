package com.clopez.chat.usermgnt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.clopez.chat.datamgnt.User;
import com.clopez.chat.datamgnt.UserDatabase;
import com.google.gson.reflect.TypeToken;

public class UDatabaseTest2 {

	private static Type type = new TypeToken<HashMap<String, User>>() {}.getType();
    private static UserDatabase db, db2;
    private static String id1, id2, id3, id4;
    private static User u1, u2, u3, u4;

    @BeforeClass
    public static void setUp() throws Exception {
        db = new UserDatabase("usertestdb", type);
        u1 = new User("clopez", "1234");
        u2 = new User("pepito", "4567");
        u3 = new User("juanito", "7890");
        id1 = u1.getId();
        id2 = u2.getId();
        id3 = u3.getId();

        db.createUser(u1);
        db.createUser(u2);
        db.createUser(u3);
    }

    @Test
    public void TestUsersFindById() {
        assertEquals("ById " + u1.getName(), (db.findById(id1)).getName(), u1.getName());
        assertEquals("ById " + u2.getName(), (db.findById(id2)).getName(), u2.getName());
        assertEquals("ById " + u3.getName(), (db.findById(id3)).getName(), u3.getName());
    }

    @Test
    public void ReloadDatabase() {
    	db2 = new UserDatabase("usertestdb", type);
    	u4 = new User("marianito", "1234");
    	id4 = u4.getId();
    	db2.createUser(u4);
    	assertEquals("ById " + u1.getName(), (db2.findById(id1)).getName(), u1.getName());
        assertEquals("ById " + u2.getName(), (db2.findById(id2)).getName(), u2.getName());
        assertEquals("ById " + u3.getName(), (db2.findById(id3)).getName(), u3.getName());
        assertEquals("ById " + u4.getName(), (db2.findById(id4)).getName(), u4.getName());
        db2.deleteUser(u4);
        assertEquals("El usuario " + u4.getName() + "No debería existir", db2.findById(u4.getId()), null);
    }
    

	
	  @AfterClass public static void CleanTests() {
	  assertEquals("Borrando la BBDD", true, db.deleteDatabase()); }
	 

}