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
    Thread-2> HSET jedis Thread-2 test => 1
    Thread-1> HGET jedis Thread-1 => test
    Thread-0> INCR count => 1
    Thread-3> HSET jedis Thread-3 test => 1
    Thread-1> INCR count => 2
    Thread-2> HGET jedis Thread-2 => test
    ...
    ...
    ...
    safeGetHInt(jedis, count) => 998
    parent> GET count => 998
    3 active resources      5 idle resources
    Thread-999> INCR count => 999
    Thread-998> INCR count => 1000
    safeGetHInt(jedis, count) => 1000
    parent> DEL jedis => 1
    Threads launched => 1000
    Fail/Retry total => 10