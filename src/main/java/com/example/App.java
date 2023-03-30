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
        // underdimension to force some queuing behaviour
        poolConfig.setMaxTotal(Integer.parseInt(prop.getProperty("pc.maxtotal")));
        //poolConfig.setMaxIdle(Integer.parseInt(prop.getProperty("ts.max")) / 2);
        //poolConfig.setMinIdle(Integer.parseInt(prop.getProperty("pc.minidle")));
        
        JedisPool pool = new JedisPool(
            poolConfig, 
            prop.getProperty("rs.host"),
            Integer.parseInt(prop.getProperty("rs.port")));
        String auth = prop.getProperty("rs.auth");

        ArrayList<Jedis> resources = new ArrayList<Jedis>();
        for (int i = 0; i < Integer.parseInt(prop.getProperty("ts.max")); i++ ) {
            // don't trust in try to close as threads getting crossed up 
            Thread hwThread = new Thread(() -> { 
                helloWorld(pool.getResource(), auth);
            });
            hwThread.start();
            //resourceInfo(prop, pool);
        }
        
        Jedis jedis = pool.getResource();
        
        jedis.auth(auth);
        if (pingWait(jedis)) {
            int count = safeGetInt(jedis, "count");
            while (count < Integer.parseInt(prop.getProperty("ts.max"))) { 
                System.out.printf("parent> GET %s => %s\n", "count", count);
                resourceInfo(prop, pool);
                waitms(100);
                count = safeGetInt(jedis, "count");
            }
        }

        System.out.printf("parent> DEL count => %s\n", jedis.del("count"));

        pool.close();
    }

    private static int safeGetInt(Jedis jedis, String key) {
        String v = jedis.get(key);
        System.out.printf("safeGetInt(%s) => %s\n", key, v);
        return v != null ? Integer.parseInt(v) : 0;
    }

    private static void helloWorld(Jedis jedis, String auth) {
        String t = Thread.currentThread().getName();
       
        jedis.auth(auth);
        if (pingWait(jedis)) {
            //String k = "hello";
            String k = t;
            String v = "world";
            //System.out.printf("%s> INCR lock => %s\n", t, jedis.incr("lock"));
            System.out.printf("%s> SET %s %s => %s\n", t, k, v, jedis.set(k, v));
            System.out.printf("%s> GET %s => %s\n", t, k, jedis.get(k));
            //System.out.printf("%s> INCRBY lock -1 => %s\n", t, jedis.incrBy("lock", -1));
            // nope, the lock can transition through 0 without completing all threads
            System.out.printf("%s> INCR count => %s\n", t, jedis.incr("count"));
        }
        jedis.close();
    }

    private static boolean pingWait(Jedis jedis) {
        String response = jedis.ping();
        int retries = 10;
        int i = 1;
        while (!response.equals("PONG") && i <= retries) {
            waitms(100);
            response = jedis.ping();
            i++;
        }
        if (i > 1) {
            System.out.printf(
                "pingWait got [%s] after %d attempts\n",
                response,
                i);
        }
        return response.equals("PONG");
    }

    private static void waitms(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void resourceInfo(Properties prop, JedisPool pool) {
        System.out.printf(
            "%d active resources\t%d idle resources\n", 
            pool.getNumActive(),
            pool.getNumIdle());
    }
}
