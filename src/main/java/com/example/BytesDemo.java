package com.example;

import redis.clients.jedis.Jedis;

public class BytesDemo {
    public static void main(String[] args) {
        Jedis jedis = new Jedis();
        String k = "ORIGINAL";
        String v = "豐盛戶售後服務-提供市場訊息";
        System.out.printf("> SET %s %s\n%s\n", k, v, jedis.set(k, v));
        System.out.printf("> GET %s\n%s\n", k, jedis.get(k));
        jedis.close();
    }
}
