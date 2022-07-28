package com.clopez.chat.usermgnt;

import static org.junit.Assert.*;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.clopez.chat.datamgnt.Message;
import com.clopez.chat.datamgnt.OffLineDatabase;
import com.clopez.chat.datamgnt.OffMessage;
import com.clopez.chat.datamgnt.User;
import com.clopez.chat.datamgnt.UserDatabase;
import com.google.gson.reflect.TypeToken;


public class OffLineMessagesTest {

	private static Type type = new TypeToken<HashMap<String, OffMessage>>() {}.getType();
	private static OffLineDatabase oldb;
    private static User u1, u2, u3;
    private static Message m1,m2,m3,m4,m5;

    @BeforeClass
    public static void setUp() throws Exception {
        oldb = new OffLineDatabase("offlinetestdb", type);
        u1 = new User("clopez", "1234");
        u2 = new User("pepito", "4567");
        u3 = new User("juanito", "7890");
        m1 = new Message("TEXT", "123", "pepito", "clopez", "Hola pepito soy clopez");
        m2 = new Message("TEXT", "124", "pepito", "clopez", "Hola pepito soy clopez otra vez");
        m3 = new Message("TEXT", "125", "pepito", "juanito", "Hola pepito soy juanito");
        m4 = new Message("TEXT", "126", "clopez", "juanito", "Hola clopez soy juanito");
        m5 = new Message("TEXT", "127", "juanito", "pepito", "Hola juanito soy pepito");
        
        oldb.addMessage(m1);
        oldb.addMessage(m2);
        oldb.addMessage(m3);
        oldb.addMessage(m4);
        oldb.addMessage(m5);
    }

    @Test
    public void TestMessagesByUser() {
        assertEquals(u1.getName() + "Tiene 1 mensaje", 1, oldb.messagesOfUser(u1).size());
        assertEquals(u2.getName() + "Tiene 3 mensajes", 3, oldb.messagesOfUser(u2).size());
        assertEquals(u3.getName() + "Tiene 1 mensaje", 1, oldb.messagesOfUser(u3).size());
        assertEquals("3 usuarios en la BBDD", 3, oldb.numberOfUsers());
        assertEquals("5 mensajes en la cola", 5, oldb.numberOfMessages());
    }

    @Test
    public void TestDelivered() {
    	List<Message> l = oldb.messagesOfUser(u2);
    	assertEquals("Debe de haber 3 mensajes", 3, l.size());
    	l.get(0).setDelivered();
    	l.get(1).setDelivered();
    	assertEquals("Borrando delivered", 2, oldb.deleteDeliveredForUser(u2));
    	assertEquals("Ahora " + u2.getName() + " debe de tener solo 1 mensaje en la cola", 1, oldb.messagesOfUser(u2).size());
    	assertEquals("Y solo deben de quedar 3 mensajes en total", 3, oldb.numberOfMessages());

    	List<Message> l2 = oldb.messagesOfUser(u2);
    	assertEquals("Debe de haber 1 mensajes", 1, l2.size());
    	l2.get(0).setDelivered();
    	oldb.deleteDeliveredForUser(u2);
    	assertEquals("Ahora " + u2.getName() + "debe de tener 0 mensajes en la cola", 0, oldb.messagesOfUser(u2).size());
    	assertEquals("Y solo deben de quedar 2 mensajes en total", 2, oldb.numberOfMessages());
    	assertEquals("Y solo 2 usuarios en la BBDD", 2, oldb.numberOfUsers());
    }

	
	  @AfterClass
	  public static void CleanTests() {
	  assertEquals("Borrando la BBDD", true, oldb.deleteDatabase()); }
	 
}
