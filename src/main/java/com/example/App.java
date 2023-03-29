package com.example;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        try (InputStream input = new FileInputStream("src/main/resources/config.properties")) {

            Properties prop = new Properties();
            prop.load(input);
            managePool(prop);

        } catch (IOException ex) {
            ex.printStackTrace();
        } 
    }

    private static void managePool(Properties prop) {
        GenericObjectPoolConfig<Jedis> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(Integer.parseInt(prop.getProperty("ts.max")));
        poolConfig.setMinIdle(Integer.parseInt(prop.getProperty("pc.minidle")));
        
        JedisPool pool = new JedisPool(
            poolConfig, 
            prop.getProperty("rs.host"),
            Integer.parseInt(prop.getProperty("rs.port")));
        String auth = prop.getProperty("rs.auth");

        ArrayList<Jedis> jedis = new ArrayList<Jedis>();
        for (int i = 0; i < Integer.parseInt(prop.getProperty("ts.max")); i++ ) {
            jedis.add(pool.getResource());
        }

        for (Jedis j : jedis) {
            resourceInfo(prop, pool);
            
            try (Jedis myJedis = j) {
                Thread hwThread = new Thread(() -> { 
                    helloWorld(myJedis, auth);
                });
                hwThread.start(); 
            } catch (Exception e) {
                e.printStackTrace();
            } 
        }

        pool.close();
    }

    private static void helloWorld(Jedis jedis, String auth) {
        jedis.auth(auth);
        String response = jedis.ping();
        System.out.printf("> PING\n%s\n", response);
        if (response.equals("PONG")) {
            //String k = "hello";
            String v = "world";
            String k = Thread.currentThread().getName();
            System.out.printf("> SET %s %s\n%s\n", k, v, jedis.set(k, v));
            System.out.printf("> GET %s\n%s\n", k, jedis.get(k));
        } else {
            System.out.printf("PING returned [%s] skipping\n", response);    
        }
    }

    private static void resourceInfo(Properties prop, JedisPool pool) {
        System.out.printf(
            //"Endpoint %s:%s has:\n%d active resources\n%d idle resources\n", 
            "%d active resources\t%d idle resources\n", 
            //prop.getProperty("rs.host"), 
            //prop.getProperty("rs.port"),
            pool.getNumActive(),
            pool.getNumIdle());
    }
}
