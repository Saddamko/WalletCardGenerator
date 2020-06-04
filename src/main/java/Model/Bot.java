/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

/**
 * Синглтон-класс Bot.
 * Для чистоты эксперимента это будет синглтон.
 * 
 * Created by promoscow on 26.07.17.
 */
public class Bot {
    private Integer id;
    private String name;
    private String serial;

    private static Bot ourInstance = new Bot();
    public static Bot getInstance() {
        return ourInstance;
    }

    private Bot() {
    }

    public Bot(String name, String serial, Integer id) {
        this.name = name;
        this.serial = serial;
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    @Override
    public String toString() {
        return "Bot{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", serial=" + serial +
                '}';
    }
}
