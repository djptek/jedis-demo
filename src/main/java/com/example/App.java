package com.example;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
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
        try (InputStream input = new FileInputStream("jedis-demo/src/main/resources/config.properties")) {

            Properties prop = new Properties();
            prop.load(input);
            managePool(prop);

        } catch (IOException ex) {
            ex.printStackTrace();
        } 
    }

    private static void managePool(Properties prop) {
        GenericObjectPoolConfig<Jedis> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(Integer.parseInt(prop.getProperty("pc.maxtotal")));
        
        System.out.printf(
            "Target => %s:%s\n", 
            System.getenv("RS_HOST"),
            System.getenv("RS_PORT"));

        JedisPool pool = new JedisPool(
            poolConfig, 
            System.getenv("RS_HOST"),
            Integer.parseInt(System.getenv("RS_PORT")));
        pool.setMaxWait(Duration.ofSeconds(10));
        String auth = System.getenv("RS_AUTH");

        int i = 0; 
        int f = 0;
        while (i < Integer.parseInt(prop.getProperty("ts.max"))) {
            // don't trust in try with resources to close as last two threads were getting crossed up?
            try {
                Jedis j = pool.getResource();
                Thread hwThread = new Thread(() -> { 
                    helloWorld(j, auth);
                });
                hwThread.start();
                i++;
            } catch (redis.clients.jedis.exceptions.JedisConnectionException e) {
                System.out.printf("Failed to connect to any host resolved for DNS name :(\n");
                f++;
            }
        }
        
        Jedis jedis = pool.getResource();
        
        jedis.auth(auth);
        if (pingWait(jedis)) {
            int count = safeGetHInt(jedis, "jedis", "count");
            while (count < Integer.parseInt(prop.getProperty("ts.max"))) { 
                System.out.printf("parent> GET %s => %s\n", "count", count);
                resourceInfo(prop, pool);
                waitms(100);
                count = safeGetHInt(jedis, "jedis", "count");
            }
        }

        System.out.printf("parent> DEL jedis => %s\n", jedis.del("jedis"));
        pool.close();
        System.out.printf("Threads launched => %d\nFail/Retry total => %d\n", i, f);
    }

    private static int safeGetHInt(Jedis jedis, String key, String field) {
        String v = jedis.hget(key, field);
        System.out.printf("safeGetHInt(%s, %s) => %s\n", key, field, v);
        return v != null ? Integer.parseInt(v) : 0;
    }

    private static void helloWorld(Jedis jedis, String auth) {
        String t = Thread.currentThread().getName();
       
        jedis.auth(auth);
        if (pingWait(jedis)) {
            String k = "jedis";
            String v = "test";
            String c = "count";
            //System.out.printf("%s> INCR lock => %s\n", t, jedis.incr("lock"));
            System.out.printf("%s> HSET %s %s %s => %s\n", t, k, t, v, jedis.hset(k, t, v));
            System.out.printf("%s> HGET %s %s => %s\n", t, k, t, jedis.hget(k, t));
            //System.out.printf("%s> INCRBY lock -1 => %s\n", t, jedis.incrBy("lock", -1));
            // nope, the lock can transition through 0 without completing all threads
            System.out.printf("%s> HINCRBY %s %s %d => %s\n", t, k, c, 1, jedis.hincrBy(k, c, 1));
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
