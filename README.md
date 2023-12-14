# jedis-demo
Jedis Demo code

Please set the following environment variables

    RS_HOST="<your-redis-endpoint>"
    RS_PORT=<your-redis-port>
    RS_AUTH="<password-for-default-redis-user"
    export RS_HOST
    export RS_PORT
    export RS_AUTH

Number of Threads + Size of Redis Pool is configurable in 

`jedis-demo/src/main/resources/config.properties`

Example output:


    Target => redis-nnnnn.xx.xx-xxxxxxxx-x.xxx.xxxxx.xxxxxxxxx.xxx:nnnnn
    Thread-0> HSET jedis Thread-0 test => 1
    Thread-1> HSET jedis Thread-1 test => 1
    Thread-0> HGET jedis Thread-0 => test
    Thread-1> HGET jedis Thread-1 => test
    Thread-2> HSET jedis Thread-2 test => 1
    Thread-0> HINCRBY jedis count 1 => 1
    Thread-1> HINCRBY jedis count 1 => 2
    Thread-3> HSET jedis Thread-3 test => 1
    Thread-2> HGET jedis Thread-2 => test
    ...
    ...
    ...
    Thread-907> HINCRBY jedis count 1 => 912
    Failed to connect to any host resolved for DNS name :(
    Thread-913> HSET jedis Thread-913 test => 1
    ...
    ...
    ...
    Thread-988> HINCRBY jedis count 1 => 992
    12 active resources     8 idle resources
    Thread-999> HSET jedis Thread-999 test => 1
    ...
    ...
    ...
    Thread-999> HGET jedis Thread-999 => test
    safeGetHInt(jedis, count) => 997
    parent> GET count => 997
    4 active resources      8 idle resources
    Thread-999> HINCRBY jedis count 1 => 999
    Thread-998> HINCRBY jedis count 1 => 1000
    Thread-997> HINCRBY jedis count 1 => 998
    safeGetHInt(jedis, count) => 1000
    parent> DEL jedis => 1
    Threads launched => 1000
    Fail/Retry total => 17# redis-jedis-scratch
