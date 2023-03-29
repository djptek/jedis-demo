package com.example;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
            helloWorld(prop);

        } catch (IOException ex) {
            ex.printStackTrace();
        } 
    }

    private static void helloWorld(Properties prop) {
        GenericObjectPoolConfig<Jedis> poolConfig = new GenericObjectPoolConfig<>();
        JedisPool pool = new JedisPool(
            poolConfig, 
            prop.getProperty("rs.host"),
            Integer.parseInt(prop.getProperty("rs.port")));

        resourceInfo(prop, pool);

        try (Jedis jedis = pool.getResource()) {
            resourceInfo(prop, pool);
            jedis.auth(prop.getProperty("rs.auth"));
            String response = jedis.ping();
            System.out.printf("> PING\n%s\n", response);
            if (response.equals("PONG")) {
                String k = "hello";
                String v = "world";
                System.out.printf("> SET %s %s\n%s\n", k, v, jedis.set(k, v));
                System.out.printf("> GET %s\n%s\n", k, jedis.get(k));
            } else {
                System.out.printf("PING returned [%s] skipping\n", response);    
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
      
        resourceInfo(prop, pool);

        pool.close();
    }

    private static void resourceInfo(Properties prop, JedisPool pool) {
        System.out.printf(
            "Endpoint %s:%s\nhas %d active resources\n", 
            prop.getProperty("rs.host"), 
            prop.getProperty("rs.port"),
            pool.getNumActive());
    }
}
