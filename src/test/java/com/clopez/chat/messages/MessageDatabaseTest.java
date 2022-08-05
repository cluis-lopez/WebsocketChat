package com.clopez.chat.messages;

import static org.junit.Assert.*;

import java.io.File;
import java.time.Instant;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.clopez.chat.datamgnt.Group;
import com.clopez.chat.datamgnt.GroupDatabase;
import com.clopez.chat.datamgnt.User;

public class MessageDatabaseTest {

	private static MessageDatabase mdb, mdb2;
	private static Message m1,m2,m3,m4;

	@BeforeClass
	public static void setUp() throws Exception {
		// public Message(String type, String mid, String to, String from, String content)
		m1 = new Message("TEXT", "1", "pepito", "clopez", "Hola pepito soy clopez");
		m2 = new Message("TEXT", "2", "clopez", "pepito", "Hola clopez soy pepito");
		m3 = new Message("TEXT", "3", "pepito", "clopez", "Hola pepito soy clopez de nuevo");
		m4 = new Message("TEXT", "4", "otropavo", "pepito", "Hola otro pavo");
		
		System.out.println("Borrando el fichero del a BBDD");
		File file = new File("webapps/messages/messages_0");
		System.out.println("El fichero: " + file.getAbsolutePath() + " se va a borrar");
		file.delete();
	}
		
	@Test
	public void test() {
		mdb = new MessageDatabase();
		assertEquals("Añadiendo mensaje", true, mdb.addMessage(m1));
		assertEquals("Añadiendo mensaje", true, mdb.addMessage(m2));
		assertEquals("Añadiendo mensaje", true, mdb.addMessage(m3));
		assertEquals("Añadiendo mensaje", true, mdb.addMessage(m4));
		
		assertEquals("Cuatro mensajes en total", 4, mdb.getNumMessages());
		assertEquals("Dos conversaciones", 2, mdb.getNumChats());
		assertEquals("Tres mensajes entre clopez y pepito", 3, mdb.getChatById(m1.getChatId()).size());
		
		mdb2 = new MessageDatabase(); //Re-Open the DDBB)
		assertEquals("Cuatro mensajes en total", 4, mdb2.getNumMessages());
		assertEquals("Dos conversaciones", 2, mdb2.getNumChats());
		assertEquals("Tres mensajes entre clopez y pepito",3, mdb2.getChatById(m1.getChatId()).size());
		
		List<Message> li = mdb.getChatById(m1.getChatId());
		Instant ins = li.get(li.size()-1).getCreated();
		System.out.println("Creado en: " + ins);
		assertTrue("Los mensajes se crearon antes", Instant.now().compareTo(ins)>1);
		
	}
}
