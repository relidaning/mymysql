-- init.sql
CREATE USER 'exporter'@'%' IDENTIFIED BY 'exporter';
GRANT ALL PRIVILEGES ON *.* TO 'exporter'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;


