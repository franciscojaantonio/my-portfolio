-- phpMyAdmin SQL Dump
-- version 5.0.2
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Tempo de geração: 16-Maio-2022 às 17:39
-- Versão do servidor: 10.4.14-MariaDB
-- versão do PHP: 7.4.10

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Banco de dados: `pisid`
--

DELIMITER $$
--
-- Procedimentos
--
CREATE DEFINER=`root`@`localhost` PROCEDURE `criarcultura` (IN `nome` VARCHAR(50), IN `zonaID` INT, IN `userID` INT, IN `descricao` VARCHAR(255), IN `temMin` DECIMAL(5,2), IN `temMax` DECIMAL(5,2), IN `humMin` DECIMAL(5,2), IN `humMax` DECIMAL(5,2), IN `luzMin` DECIMAL(5,2), IN `luzMax` DECIMAL(5,2))  BEGIN

INSERT INTO cultura 
(Nome, IDZona, IDUtilizador, Descricao)VALUES (nome, zonaID, userID, descricao);

INSERT INTO parametrocultura (IDCultura,TemperaturaMinima, TemperaturaMaxima, HumidadeMinima, HumidadeMaxima, LuzMinima, LuzMaxima) VALUES (LAST_INSERT_ID(),temMin, temMax, humMin, humMax, luzMin, luzMax);

END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `criarutilizador` (IN `nome` VARCHAR(100), IN `email` VARCHAR(100), IN `pass` VARCHAR(100), IN `tipo` INT)  BEGIN 
DECLARE _HOST CHAR(14) DEFAULT '@\'localhost\'';
DECLARE _ROLE VARCHAR(20);

INSERT INTO utilizador (Nome, Email, IDTipoUtilizador)
VALUES (nome, email, tipo);

IF tipo = 1 THEN
	SET _ROLE = "Investigador";
    ELSEIF tipo = 2 THEN SET _ROLE = "Tecnico";
END IF;


SET 
nome := CONCAT('\'', REPLACE(TRIM(nome), CHAR(39), CONCAT(CHAR(92), CHAR(39))), '\''),
pass := CONCAT('\'', REPLACE(pass, CHAR(39), CONCAT(CHAR(92), CHAR(39))), '\'');

SET @sql := CONCAT('CREATE USER ', nome, _HOST, ' IDENTIFIED BY ', pass);
 
PREPARE stmt FROM @sql;
EXECUTE stmt;

SET @sql := CONCAT('GRANT ',_ROLE,' TO ', nome, _HOST);
PREPARE stmt FROM @sql;
EXECUTE stmt;
SET @sql := CONCAT('SET DEFAULT ROLE ',_ROLE,' FOR ', nome,_HOST );
PREPARE stmt FROM @sql;
EXECUTE stmt;


END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `removercultura` (IN `culturaID` INT)  BEGIN
DELETE FROM cultura
WHERE IDCultura = culturaID;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `removerutilizador` (IN `nome` VARCHAR(50))  BEGIN 
DECLARE _HOST CHAR(14) DEFAULT '@\'localhost\'';
DECLARE id_utilizador INT DEFAULT -1;

SELECT u.IDUtilizador INTO id_utilizador
	FROM utilizador as u
    	WHERE u.Nome = nome;

IF id_utilizador <> -1 THEN 
	SET nome := CONCAT('\'', REPLACE(TRIM(nome), CHAR(39), CONCAT(CHAR(92), CHAR(39))), '\'');

	SET @sql := CONCAT('DROP USER ', nome, _HOST);
 
	PREPARE stmt FROM @sql;
	EXECUTE stmt;

	UPDATE cultura as c SET c.IDUtilizador = NULL
    	WHERE c.IDUtilizador = id_utilizador;

	DELETE FROM utilizador
	WHERE IDUtilizador = id_utilizador;
END IF;
END$$

--
-- Funções
--
CREATE DEFINER=`root`@`localhost` FUNCTION `isAlertaOK` (`id_cultura` INT(10), `id_severidade` INT(10), `data` TIMESTAMP) RETURNS INT(11) NO SQL
BEGIN
DECLARE valor INT;
DECLARE sec INT;
DECLARE lastTime TIMESTAMP;

SELECT s.Tempo INTO sec
	FROM severidade as s 
    	WHERE s.IDSeveridade=id_severidade;
        
SELECT a.Data INTO lastTime FROM alerta as a 
	WHERE 
    a.IDCultura=id_cultura AND a.IDSeveridade=id_severidade
    	ORDER BY a.IDAlerta DESC LIMIT 1;
RETURN TIMESTAMPDIFF(SECOND,lastTime,data) > sec;
END$$

CREATE DEFINER=`root`@`localhost` FUNCTION `isParametrosOk` (`leitura` FLOAT(10), `id_sensor` INT, `id_cultura` INT) RETURNS INT(10) BEGIN
DECLARE nome_tipo_cultura VARCHAR(20);
DECLARE ID_Tipo INT;
DECLARE lim5 FLOAT;
DECLARE lim10 FLOAT;
DECLARE lim_sup FLOAT;
DECLARE lim_inf FLOAT;

SELECT s.IDTipoSensor
	INTO ID_Tipo
    	FROM sensor as s
        	WHERE s.IDSensor = id_sensor;
SELECT ts.Nome
	INTO nome_tipo_cultura		
    	FROM tiposensor as ts
			WHERE ts.IDTipo = ID_Tipo; 
    
IF nome_tipo_cultura = 'Humidade' THEN
	SELECT pc.Hum5, pc.Hum10, pc.HumidadeMinima, pc.HumidadeMaxima 
		INTO lim5, lim10, lim_inf, lim_sup 
			FROM parametrocultura as pc
				WHERE pc.IDCultura = id_cultura;     
ELSEIF nome_tipo_cultura = 'Temperatura' THEN
	SELECT pc.Tem5,pc.Tem10,pc.TemperaturaMinima,pc.TemperaturaMaxima 
		INTO lim5, lim10, lim_inf, lim_sup 
			FROM parametrocultura as pc
				WHERE pc.IDCultura = id_cultura; 
ELSEIF nome_tipo_cultura = 'Luz' THEN
	SELECT pc.Luz5, pc.Luz10, pc.LuzMinima, pc.LuzMaxima 
		INTO lim5, lim10, lim_inf, lim_sup  
			FROM parametrocultura as pc
				WHERE pc.IDCultura = id_cultura; 
END IF; 

IF leitura > lim_sup OR leitura < lim_inf THEN
		RETURN 3;
ELSEIF leitura > lim_sup - lim5 OR leitura < lim_inf + lim5 THEN
		RETURN 2;
ELSEIF leitura > lim_sup - lim10 OR leitura < lim_inf + lim10 THEN
		RETURN 1;
END IF;
RETURN 0;

END$$

DELIMITER ;

-- --------------------------------------------------------

--
-- Estrutura da tabela `alerta`
--

CREATE TABLE `alerta` (
  `IDAlerta` int(10) NOT NULL,
  `IDMedicao` int(10) NOT NULL,
  `IDCultura` int(10) NOT NULL,
  `IDSeveridade` int(10) NOT NULL,
  `Data` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `Mensagem` varchar(100) COLLATE utf8_bin DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- --------------------------------------------------------

--
-- Estrutura da tabela `cultura`
--

CREATE TABLE `cultura` (
  `IDCultura` int(10) NOT NULL,
  `IDZona` int(10) NOT NULL,
  `IDUtilizador` int(10) DEFAULT NULL,
  `Nome` varchar(20) COLLATE utf8_bin NOT NULL,
  `Descricao` varchar(100) COLLATE utf8_bin DEFAULT NULL,
  `LastUpdate` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- --------------------------------------------------------

--
-- Estrutura da tabela `medicao`
--

CREATE TABLE `medicao` (
  `IDMedicao` int(10) NOT NULL,
  `IDSensor` int(10) NOT NULL,
  `IDZona` int(10) NOT NULL,
  `Data` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `LeituraCorreta` tinyint(1) NOT NULL,
  `Leitura` decimal(5,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

--
-- Acionadores `medicao`
--
DELIMITER $$
CREATE TRIGGER `trAlarme` AFTER INSERT ON `medicao` FOR EACH ROW BEGIN
DECLARE valor INT;
DECLARE canAdd INT;
DECLARE done INT DEFAULT FALSE;
DECLARE intV integer;
DECLARE isCorrect BIT DEFAULT 0; 
DECLARE cur CURSOR FOR SELECT IDCultura FROM cultura WHERE new.IDZona=IDZona;
DECLARE
    CONTINUE HANDLER FOR NOT FOUND
SET
    done = TRUE;
    
    IF new.LeituraCorreta THEN
OPEN cur;
teams_loop:
LOOP
	FETCH cur INTO intV;
IF done THEN LEAVE teams_loop;
END IF;

SELECT isParametrosOk(new.Leitura, new.IDSensor, intV) INTO valor;

IF valor != 0 THEN
	SELECT isAlertaOK(intV, valor, CURRENT_TIMESTAMP) INTO canAdd;
	IF canAdd OR ISNULL(canAdd) THEN
    INSERT INTO alerta (IDMedicao,IDCultura,IDSeveridade,Data,Mensagem)
    VALUES(new.IDMedicao,intV,valor, CURRENT_TIMESTAMP,canAdd);
    END IF;
END IF;
END
LOOP
    teams_loop;
CLOSE cur;
    END IF;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Estrutura da tabela `parametrocultura`
--

CREATE TABLE `parametrocultura` (
  `IDCultura` int(10) NOT NULL,
  `TemperaturaMinima` decimal(5,2) NOT NULL,
  `TemperaturaMaxima` decimal(5,2) NOT NULL,
  `HumidadeMinima` decimal(5,2) NOT NULL,
  `HumidadeMaxima` decimal(5,2) NOT NULL,
  `LuzMinima` decimal(5,2) NOT NULL,
  `LuzMaxima` decimal(5,2) NOT NULL,
  `Tem5` decimal(5,2) DEFAULT NULL,
  `Tem10` decimal(5,2) DEFAULT NULL,
  `Hum5` decimal(5,2) DEFAULT NULL,
  `Hum10` decimal(5,2) DEFAULT NULL,
  `Luz5` decimal(5,2) DEFAULT NULL,
  `Luz10` decimal(5,2) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

--
-- Acionadores `parametrocultura`
--
DELIMITER $$
CREATE TRIGGER `trInsertParametros` BEFORE INSERT ON `parametrocultura` FOR EACH ROW SET 
new.Tem5 = 0.05*(new.TemperaturaMaxima - new.TemperaturaMinima),
	new.Tem10 = 2 * new.Tem5,
	new.Hum5 = 0.05*(new.HumidadeMaxima - new.HumidadeMinima),
	new.Hum10 = 2 * new.Hum5,
	new.Luz5 = 0.05*(new.LuzMaxima - new.LuzMinima),
	new.Luz10 = 2 * new.Luz5
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `trUpdateParametros` BEFORE UPDATE ON `parametrocultura` FOR EACH ROW BEGIN
SET 
new.Tem5 = 0.05*(new.TemperaturaMaxima - new.TemperaturaMinima),
	new.Tem10 = 2 * new.Tem5,
	new.Hum5 = 0.05*(new.HumidadeMaxima - new.HumidadeMinima),
	new.Hum10 = 2 * new.Hum5,
	new.Luz5 = 0.05*(new.LuzMaxima - new.LuzMinima),
	new.Luz10 = 2 * new.Luz5;

UPDATE cultura AS c
 	SET c.LastUpdate = CURRENT_TIMESTAMP()
    Where c.IDCultura = new.IDCultura;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Estrutura da tabela `sensor`
--

CREATE TABLE `sensor` (
  `IDSensor` int(10) NOT NULL,
  `Nome` varchar(20) COLLATE utf8_bin NOT NULL,
  `IDTipoSensor` int(10) NOT NULL,
  `IDZona` int(10) NOT NULL,
  `LimiteInferior` decimal(5,2) NOT NULL,
  `LimiteSuperior` decimal(5,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

--
-- Extraindo dados da tabela `sensor`
--

INSERT INTO `sensor` (`IDSensor`, `Nome`, `IDTipoSensor`, `IDZona`, `LimiteInferior`, `LimiteSuperior`) VALUES
(1, 'H1', 1, 1, '0.00', '100.00'),
(2, 'H2', 1, 2, '0.00', '90.00'),
(3, 'L1', 2, 1, '0.00', '500.00'),
(4, 'L2', 2, 2, '0.00', '300.00'),
(5, 'T1', 3, 1, '0.00', '45.00'),
(6, 'T2', 3, 2, '2.00', '50.00');

-- --------------------------------------------------------

--
-- Estrutura da tabela `severidade`
--

CREATE TABLE `severidade` (
  `IDSeveridade` int(10) NOT NULL,
  `Nome` varchar(20) COLLATE utf8_bin NOT NULL,
  `Descricao` varchar(50) COLLATE utf8_bin DEFAULT NULL,
  `Tempo` int(10) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

--
-- Extraindo dados da tabela `severidade`
--

INSERT INTO `severidade` (`IDSeveridade`, `Nome`, `Descricao`, `Tempo`) VALUES
(1, 'leve', 'severidade leve', 600),
(2, 'grave', 'severidade grave', 300),
(3, 'muito grave', 'severidade muito grave', 120);

-- --------------------------------------------------------

--
-- Estrutura da tabela `tiposensor`
--

CREATE TABLE `tiposensor` (
  `IDTipo` int(10) NOT NULL,
  `Nome` varchar(20) COLLATE utf8_bin NOT NULL,
  `Descricao` varchar(50) COLLATE utf8_bin DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

--
-- Extraindo dados da tabela `tiposensor`
--

INSERT INTO `tiposensor` (`IDTipo`, `Nome`, `Descricao`) VALUES
(1, 'Humidade', 'Sensor do tipo Humidade'),
(2, 'Luz', 'Sensor do tipo Luz'),
(3, 'Temperatura', 'Sensor do tipo Temperatura');

-- --------------------------------------------------------

--
-- Estrutura da tabela `tipoutilizador`
--

CREATE TABLE `tipoutilizador` (
  `IDTipoUtilizador` int(10) NOT NULL,
  `Nome` varchar(50) COLLATE utf8_bin NOT NULL,
  `Descricao` varchar(100) COLLATE utf8_bin DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

--
-- Extraindo dados da tabela `tipoutilizador`
--

INSERT INTO `tipoutilizador` (`IDTipoUtilizador`, `Nome`, `Descricao`) VALUES
(1, 'Investigador', 'Utilizador responsável por uma ou mais culturas'),
(2, 'Técnico de Manutenção', 'Utilizador responsável pela manutenção da base de dados');

-- --------------------------------------------------------

--
-- Estrutura da tabela `utilizador`
--

CREATE TABLE `utilizador` (
  `IDUtilizador` int(10) NOT NULL,
  `Nome` varchar(20) COLLATE utf8_bin NOT NULL,
  `Email` varchar(20) COLLATE utf8_bin NOT NULL,
  `IDTipoUtilizador` int(10) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- --------------------------------------------------------

--
-- Estrutura da tabela `zona`
--

CREATE TABLE `zona` (
  `IDZona` int(10) NOT NULL,
  `Nome` varchar(20) COLLATE utf8_bin NOT NULL,
  `TemperaturaMedia` decimal(5,2) NOT NULL,
  `HumidadeMedia` decimal(5,2) NOT NULL,
  `LuzMedia` decimal(5,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

--
-- Extraindo dados da tabela `zona`
--

INSERT INTO `zona` (`IDZona`, `Nome`, `TemperaturaMedia`, `HumidadeMedia`, `LuzMedia`) VALUES
(1, 'Z1', '12.00', '20.00', '10.00'),
(2, 'Z2', '13.00', '30.00', '20.00');

--
-- Índices para tabelas despejadas
--

--
-- Índices para tabela `alerta`
--
ALTER TABLE `alerta`
  ADD PRIMARY KEY (`IDAlerta`),
  ADD KEY `IDMedicao` (`IDMedicao`),
  ADD KEY `IDCultura` (`IDCultura`),
  ADD KEY `IDSeveridade` (`IDSeveridade`);

--
-- Índices para tabela `cultura`
--
ALTER TABLE `cultura`
  ADD PRIMARY KEY (`IDCultura`),
  ADD UNIQUE KEY `Nome` (`Nome`),
  ADD KEY `IDUtilizador` (`IDUtilizador`),
  ADD KEY `IDZona` (`IDZona`);

--
-- Índices para tabela `medicao`
--
ALTER TABLE `medicao`
  ADD PRIMARY KEY (`IDMedicao`),
  ADD KEY `IDSensor` (`IDSensor`),
  ADD KEY `IDZona` (`IDZona`);

--
-- Índices para tabela `parametrocultura`
--
ALTER TABLE `parametrocultura`
  ADD PRIMARY KEY (`IDCultura`);

--
-- Índices para tabela `sensor`
--
ALTER TABLE `sensor`
  ADD PRIMARY KEY (`IDSensor`),
  ADD UNIQUE KEY `Nome` (`Nome`),
  ADD KEY `IDTipoSensor` (`IDTipoSensor`),
  ADD KEY `IDZona` (`IDZona`);

--
-- Índices para tabela `severidade`
--
ALTER TABLE `severidade`
  ADD PRIMARY KEY (`IDSeveridade`);

--
-- Índices para tabela `tiposensor`
--
ALTER TABLE `tiposensor`
  ADD PRIMARY KEY (`IDTipo`),
  ADD UNIQUE KEY `Nome` (`Nome`);

--
-- Índices para tabela `tipoutilizador`
--
ALTER TABLE `tipoutilizador`
  ADD PRIMARY KEY (`IDTipoUtilizador`),
  ADD UNIQUE KEY `Nome` (`Nome`);

--
-- Índices para tabela `utilizador`
--
ALTER TABLE `utilizador`
  ADD PRIMARY KEY (`IDUtilizador`),
  ADD UNIQUE KEY `Nome` (`Nome`),
  ADD KEY `IDTipoUtilizador` (`IDTipoUtilizador`);

--
-- Índices para tabela `zona`
--
ALTER TABLE `zona`
  ADD PRIMARY KEY (`IDZona`),
  ADD UNIQUE KEY `Nome` (`Nome`);

--
-- AUTO_INCREMENT de tabelas despejadas
--

--
-- AUTO_INCREMENT de tabela `alerta`
--
ALTER TABLE `alerta`
  MODIFY `IDAlerta` int(10) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de tabela `cultura`
--
ALTER TABLE `cultura`
  MODIFY `IDCultura` int(10) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de tabela `medicao`
--
ALTER TABLE `medicao`
  MODIFY `IDMedicao` int(10) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de tabela `utilizador`
--
ALTER TABLE `utilizador`
  MODIFY `IDUtilizador` int(10) NOT NULL AUTO_INCREMENT;

--
-- Restrições para despejos de tabelas
--

--
-- Limitadores para a tabela `alerta`
--
ALTER TABLE `alerta`
  ADD CONSTRAINT `alerta_ibfk_1` FOREIGN KEY (`IDCultura`) REFERENCES `cultura` (`IDCultura`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `alerta_ibfk_2` FOREIGN KEY (`IDMedicao`) REFERENCES `medicao` (`IDMedicao`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `alerta_ibfk_3` FOREIGN KEY (`IDSeveridade`) REFERENCES `severidade` (`IDSeveridade`) ON UPDATE CASCADE;

--
-- Limitadores para a tabela `cultura`
--
ALTER TABLE `cultura`
  ADD CONSTRAINT `cultura_ibfk_1` FOREIGN KEY (`IDZona`) REFERENCES `zona` (`IDZona`) ON UPDATE CASCADE,
  ADD CONSTRAINT `cultura_ibfk_2` FOREIGN KEY (`IDUtilizador`) REFERENCES `utilizador` (`IDUtilizador`) ON DELETE SET NULL ON UPDATE CASCADE;

--
-- Limitadores para a tabela `medicao`
--
ALTER TABLE `medicao`
  ADD CONSTRAINT `medicao_ibfk_1` FOREIGN KEY (`IDSensor`) REFERENCES `sensor` (`IDSensor`) ON UPDATE CASCADE,
  ADD CONSTRAINT `medicao_ibfk_2` FOREIGN KEY (`IDZona`) REFERENCES `zona` (`IDZona`) ON UPDATE CASCADE;

--
-- Limitadores para a tabela `parametrocultura`
--
ALTER TABLE `parametrocultura`
  ADD CONSTRAINT `parametrocultura_ibfk_1` FOREIGN KEY (`IDCultura`) REFERENCES `cultura` (`IDCultura`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Limitadores para a tabela `sensor`
--
ALTER TABLE `sensor`
  ADD CONSTRAINT `sensor_ibfk_1` FOREIGN KEY (`IDTipoSensor`) REFERENCES `tiposensor` (`IDTipo`) ON UPDATE CASCADE,
  ADD CONSTRAINT `sensor_ibfk_2` FOREIGN KEY (`IDZona`) REFERENCES `zona` (`IDZona`) ON UPDATE CASCADE;

--
-- Limitadores para a tabela `utilizador`
--
ALTER TABLE `utilizador`
  ADD CONSTRAINT `utilizador_ibfk_1` FOREIGN KEY (`IDTipoUtilizador`) REFERENCES `tipoutilizador` (`IDTipoUtilizador`) ON DELETE SET NULL ON UPDATE CASCADE;

DELIMITER $$
--
-- Eventos
--
CREATE DEFINER=`root`@`localhost` EVENT `deleteEvery24h` ON SCHEDULE EVERY 24 YEAR STARTS '2022-05-08 11:15:49' ON COMPLETION NOT PRESERVE ENABLE DO DELETE FROM medicao
WHERE DATEDIFF(CURRENT_TIMESTAMP,medicao.Data)>7$$

DELIMITER ;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
