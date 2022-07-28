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

public class OffLineDatabaseTest {

	private static Type type = new TypeToken<HashMap<String, OffMessage>>() {
	}.getType();
	private static OffLineDatabase oldb, oldb2, oldb3;
	private static User u1, u2, u3;
	private static Message m1, m2, m3, m4, m5;

	@BeforeClass
	public static void setUp() throws Exception {
		oldb = new OffLineDatabase("offlinetestdb", type);
		oldb.deleteDatabase();
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
	public void ReloadDatabase() {
		oldb2 = new OffLineDatabase("offlinetestdb", type);
		assertEquals(u1.getName() + " tiene 1 mensaje", 1, oldb2.messagesOfUser(u1).size());
		assertEquals(u2.getName() + " tiene 3 mensajes", 3, oldb2.messagesOfUser(u2).size());
		assertEquals(u3.getName() + " tiene 1 mensaje", 1, oldb2.messagesOfUser(u3).size());
		assertEquals("3 usuarios en la BBDD", 3, oldb2.numberOfUsers());
		assertEquals("5 mensajes en la cola", 5, oldb2.numberOfMessages());

		Message m6 = new Message("TEXT", "234", "clopez", "pepito", "hola Clopez soy pepito de nuevo");
		Message m7 = new Message("TEXT", "235", "clopez", "pepito", "hola Clopez soy pepito again");
		oldb2.addMessage(m6);
		oldb2.addMessage(m7);
		assertEquals(u1.getName() + "Tiene 3 mensaje", 3, oldb2.messagesOfUser(u1).size());

		oldb3 = new OffLineDatabase("offlinetestdb", type);
		List<Message> l = oldb3.messagesOfUser(u2);
		assertEquals("Pepito debe de tener 3 mensajes", 3, l.size());
		l.get(0).setDelivered();
		l.get(1).setDelivered();
		assertEquals("Borrando delivered de Pepito", 2, oldb3.deleteDeliveredForUser(u2));
		assertEquals("Ahora " + u2.getName() + " debe de tener solo 1 mensaje en la cola", 1,
				oldb3.messagesOfUser(u2).size());
		assertEquals("Y solo deben de quedar 5 mensajes en total", 5, oldb3.numberOfMessages());

		List<Message> l2 = oldb3.messagesOfUser(u2);
		assertEquals("Debe de haber 1 mensajes", 1, l2.size());
		l2.get(0).setDelivered();
		oldb3.deleteDeliveredForUser(u2);
		assertEquals("Ahora " + u2.getName() + "debe de tener 0 mensajes en la cola", 0,
				oldb3.messagesOfUser(u2).size());
		assertEquals("Y solo deben de quedar 4 mensajes en total", 4, oldb3.numberOfMessages());
		assertEquals("Y solo 2 usuarios en la BBDD", 2, oldb3.numberOfUsers());
	}

	
	  @AfterClass public static void CleanTests() {
	  assertEquals("Borrando la BBDD", true, oldb.deleteDatabase()); }
	 

}
