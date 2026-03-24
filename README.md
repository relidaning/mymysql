# MyMySQL

A testing demo for mysql, mostly for a better performance, built by docker & spring boot.

## Usage

```
cd mymysql
mvn clean package

docker-compose up -d
```

## Prerequisite

- [x] Init a Users table
- [x] Insert 10M users(17m)
- [x] Normal valocity for query
- [x] Valocity for optmized query

## Results

- Without PK: 60s+
- With PK: about 9s
- Group by Gender(without index): 13s
- Group by Gender(with index): 4s
- Add other fileds index: Same as above
