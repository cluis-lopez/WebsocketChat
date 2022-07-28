package com.clopez.chat.datamgnt;

import java.lang.reflect.Type;
import java.util.HashMap;

import com.google.gson.reflect.TypeToken;

public class Tester2 {
    public static void main(String[] args){
    	Type type = new TypeToken<HashMap<String, User>>() {}.getType();
        UserDatabase db = new UserDatabase("maindb", type);

        User u = db.findUserByName("clopez");

        if (u != null)
            System.out.println("El usuario " + u.getName() + " tiene Id " + u.getId());
        else
            System.out.println("Usuario no encontrado");

        System.out.println("Borrando la BBDD ");
        if (db.deleteDatabase())
            System.out.println("Fichero borrado");
        else
            System.out.println("No existe el fichero");
    }
}
