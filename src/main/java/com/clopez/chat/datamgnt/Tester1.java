package com.clopez.chat.datamgnt;

import java.lang.reflect.Type;
import java.util.HashMap;

import com.google.gson.reflect.TypeToken;

public class Tester1 {

    public static void main(String[] args) {
    	Type type = new TypeToken<HashMap<String, User>>() {}.getType();
        UserDatabase db = new UserDatabase("maindb", type);
        User u1 = new User("clopez", "1234");

        System.out.println("Borrando DB");
        if (db.deleteDatabase())
            System.out.println("Borrando el fichero");
        else
            System.out.println("El fichero no existe");

        System.out.println("Creando usuario de pueba");
        db.createUser(u1);

        User u2 = db.findUserByName("clopez");

        System.out.println("EL usuario " + u2.getName() + " tiene id " + u2.getId());

        User u3 = new User("pepito", "1243");

        db.createUser(u3);


    }


}
