# MyMySQL

A testing demo for mysql, mostly for a better performance, built by docker & spring boot.

## Usage

```
cd mymysql
mvn clean package

docker-compose up -d
```

## Testing

- [x] Init a Users table
- [x] Insert 10M users(17m)
- [x] Querying without index
- [x] Add index, querying
- [x] Add new portioned table
- [x] Do it, again

## Results

- Without PK: 60s+
- With PK: about 9s
- Group by Gender(without index): 13s
- Add index: gender, group by it, 4s
- The performance of querying over portioned table, is similar

## Conclusion

So, the optimization of querying over 10M data, is about 3s.
Other than that, you should consider creating a table for your statistic if exact not required, or redis
