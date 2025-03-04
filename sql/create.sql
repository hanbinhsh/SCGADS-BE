create database scMoAnnoDB;
use scMoAnnoDB;

CREATE USER 'scmoannoadmin'@'%' IDENTIFIED BY '114514';
GRANT ALL PRIVILEGES ON scMoAnnoDB.* TO 'scmoannoadmin'@'%' WITH GRANT OPTION;

