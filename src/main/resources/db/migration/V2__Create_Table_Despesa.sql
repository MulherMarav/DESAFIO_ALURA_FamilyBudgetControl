CREATE TABLE `despesas` (
  `id` int NOT NULL AUTO_INCREMENT,
  `descricao` longtext NOT NULL,
  `valor` double NOT NULL,
  `data` date NOT NULL,
  PRIMARY KEY (`id`)
);