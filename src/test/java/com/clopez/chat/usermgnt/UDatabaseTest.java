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

public class UDatabaseTest {

	private static Type type = new TypeToken<HashMap<String, User>>() {}.getType();
    private static UserDatabase db;
    private static String id1, id2, id3;
    private static User u1, u2, u3;

    @BeforeClass
    public static void setUp() throws Exception {
        db = new UserDatabase("testdb", type);
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
    public void TestUsersFindByName() {
        assertEquals("ByName " + u1.getName(), (db.findUserByName("clopez")).getId(), id1);
        assertEquals("ByName " + u2.getName(), (db.findUserByName("pepito")).getId(), id2);
        assertEquals("ByName " + u3.getName(), (db.findUserByName("juanito")).getId(), id3);
    }
    
    @Test
    public void TestRecentChats() {
    	u1.updateRecent(u2.getName());
    	assertEquals("Ultima chat es u2 ", db.findById(u1.getId()).getRecentChats()[0], u2.getName());
    	u1.updateRecent(u3.getName());
    	assertEquals("Ultima chat es u3 ", db.findById(u1.getId()).getRecentChats()[0], u3.getName());
    	for (int i=0; i<30; i++)
    		u1.updateRecent("invalido");
    	u1.updateRecent(u3.getName());
    	assertEquals("Ultima chat es u3 ", db.findById(u1.getId()).getRecentChats()[0], u3.getName());
    	assertEquals("El penultimo es invalido ", db.findById(u1.getId()).getRecentChats()[1], "invalido");
    }

    @Test
    public void DeleteUser() {
        db.deleteUser(u2);
        assertEquals("El usuario " + u2.getName() + "No debería existir", db.findById(u2.getId()), null);
    }

    @Test
    public void Passwords(){
        assertEquals("Password clopez debe de ser 1234", true, u1.passwordMatch("1234"));
        assertEquals("clopez no tiene password hola", false, u1.passwordMatch("hola"));
        assertEquals("Password juanito debe de ser 7890", true, u3.passwordMatch("7890"));
        }

    @Test
    public void Tokens(){
        assertEquals("El token de clopez debe de durar 30 días, valido hasta " + u1.getTokenValidUpTo(), true, u1.isTokenValid());
        /*long diff = u1.getTokenValidUpTo().getTime() - new Date().getTime();
        long teorico = 30*24*3600*1000;
        System.out.println("Diff : " + diff + " Teorico (30 dias)  : " + teorico);
        assertTrue("El token deben de servir para +/- 30 * 24 * 3600 * 1000 milisegundos", diff < (30*24*3600*1000)+5000 && diff > (30*24*3600*1000)-5000);
        */
    }


    @Test (expected = IllegalArgumentException.class)
    public void DuplicateUser() {
        db.createUser(u1);
    }

    @Test (expected = IllegalArgumentException.class)
    public void DeleteInvalidUser() {
    	User u4 = new User("manolo","9876");
        db.deleteUser(u4);
    }

    @AfterClass
    public static void CleanTests() {
        assertEquals("Borrando la BBDD", true, db.deleteDatabase());
    }

}