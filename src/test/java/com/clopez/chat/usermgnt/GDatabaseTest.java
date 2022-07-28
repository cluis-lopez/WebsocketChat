package com.clopez.chat.usermgnt;

import static org.junit.Assert.*;

import java.lang.reflect.Type;
import java.util.HashMap;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.clopez.chat.datamgnt.Group;
import com.clopez.chat.datamgnt.GroupDatabase;
import com.clopez.chat.datamgnt.User;
import com.google.gson.reflect.TypeToken;

public class GDatabaseTest {

	private static Type type = new TypeToken<HashMap<String, Group>>() {}.getType();
	private static GroupDatabase gdb;
	private static String id1, id2, id3;
	private static Group g1, g2, g3;
	private static User u1, u2, u3;

	@BeforeClass
	public static void setUp() throws Exception {
		gdb = new GroupDatabase("grouptestdb", type);
		u1 = new User("clopez", "1234");
		u2 = new User("pepito", "4567");
		u3 = new User("Jaimito", "7890");
		g1 = new Group("El Eco de la Lobera", u1);
		g2 = new Group("Pimenton", u1);
		g3 = new Group("Futbol", u2);
		id1 = g1.getId();
		id2 = g2.getId();
		id3 = g3.getId();

		gdb.createGroup(g1);
		gdb.createGroup(g2);
		gdb.createGroup(g3);
	}

	@Test
	public void TestGrooupFindById() {
		assertEquals("ById " + g1.getName(), (gdb.findById(id1)).getName(), g1.getName());
		assertEquals("ById " + g2.getName(), (gdb.findById(id2)).getName(), g2.getName());
		assertEquals("ById " + g3.getName(), (gdb.findById(id3)).getName(), g3.getName());
	}

	@Test
	public void TestGroupFindByName() {
		assertEquals("ByName " + g1.getName(), (gdb.findGroupByName("El Eco de la Lobera")).getId(), id1);
		assertEquals("ByName " + g2.getName(), (gdb.findGroupByName("Pimenton")).getId(), id2);
		assertEquals("ByName " + g3.getName(), (gdb.findGroupByName("Futbol")).getId(), id3);
	}

	@Test
	public void DeleteGroup() {
		gdb.deleteGroup(g3);
		assertEquals("El grupo " + g3.getName() + "No debería existir", gdb.findById(g3.getId()), null);
		gdb.createGroup(g3);
	}

	@Test
	public void Owners() {
		assertEquals("El usuario " + u1.getName() + " tiene 2 grupos", gdb.findByOwner(u1).size(), 2);
		assertEquals("El usuario " + u2.getName() + " tiene 1 grupos", gdb.findByOwner(u2).size(), 1);
	}

	@Test
	public void Users() {
		g1.addMember(u2);
		g1.addMember(u3);
		assertEquals("El grupo " + g1.getName() + " tiene 3 usuarios", (gdb.findById(g1.getId())).getNumMembers(), 3);
		assertEquals("El grupo " + g2.getName() + " tiene 1 usuario", (gdb.findById(g2.getId())).getNumMembers(), 1);
		g1.removeMember(u3);
		assertEquals("El grupo " + g1.getName() + " tiene 3 usuarios", (gdb.findById(g1.getId())).getNumMembers(), 2);
	}

	@Test(expected = IllegalArgumentException.class)
	public void DuplicateGroup() {
		gdb.createGroup(g1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void DeleteInvalidUser() {
		Group g4 = new Group("cotilleos", u3);
		gdb.deleteGroup(g4);
	}

	@AfterClass
	public static void CleanTests() {
		assertEquals("Borrando la BBDD", true, gdb.deleteDatabase());
	}

}
