package com.clopez.chat.usermgnt;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

import com.clopez.chat.datamgnt.User;
import com.clopez.chat.datamgnt.User.Chat;

public class RecentChatTest {

	private static User u1, u2, u3, u4, u5;

	@BeforeClass
	public static void setUp() throws Exception {
		u1 = new User("clopez", "1234");
		u2 = new User("pepito", "1234");
		u3 = new User("jaimito", "4567");
		u4 = new User("marianito", "8907");
		u5 = new User("gorka", "4321");
	}
	
	@Test
	public void test() {
		u1.updateRecent("pepito", true);
		u1.updateRecent("jaimito", true);
		u1.updateRecent("marianito", true);
		assertEquals("El chat más reciente debe ser marianito: ", "marianito", u1.getRecentChats()[0].getName());
		assertEquals("El siguiente debe de ser  jaimito", "jaimito", u1.getRecentChats()[1].getName());
		assertEquals("Y el ultimo debe de ser pepito ", "pepito", u1.getRecentChats()[2].getName());
		System.out.println("Test 1: " + recentString(u1.getRecentChats()));
		
		u1.updateRecent("marianito",true);
		assertEquals("2 - El chat más reciente debe ser marianito: ", "marianito", u1.getRecentChats()[0].getName());
		assertEquals("2 - El siguiente debe de ser  jaimito", "jaimito", u1.getRecentChats()[1].getName());
		assertEquals("2- Y el ultimo debe de seguir siendo pepito ", "pepito", u1.getRecentChats()[2].getName());
		System.out.println("Test 2: " + recentString(u1.getRecentChats()));
		
		u1.updateRecent("pepito", true);
		assertEquals("3 - E chat más reciente debe ser pepito: ", "pepito", u1.getRecentChats()[0].getName());
		assertEquals("3 - El siguiente debe de ser  marianito", "marianito", u1.getRecentChats()[1].getName());
		assertEquals("3- Y el ultimo debe de seguir siendo jaimito ", "jaimito", u1.getRecentChats()[2].getName());
		System.out.println("Test 3: " + recentString(u1.getRecentChats()));
		
		u1.updateRecent("gorka", true);
		assertEquals("3 - E chat más reciente debe ser gorka: ", "gorka", u1.getRecentChats()[0].getName());
		assertEquals("3 - El siguiente debe de ser  pepito", "pepito", u1.getRecentChats()[1].getName());
		assertEquals("3- Y el ultimo ahora tienen que ser marianito ", "marianito", u1.getRecentChats()[2].getName());
		System.out.println("Test 3: " + recentString(u1.getRecentChats()));
		
	}
	
	private String recentString(Chat[] kk) {
		String ss= "[";
		for (int i=0; i<kk.length; i++)
			ss += "'"+kk[i].getName()+"',";
		ss += "]";
		return ss;
	}

}
