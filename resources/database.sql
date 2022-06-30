CREATE database login CHARACTER SET 'utf8mb4';

CREATE USER login@'%' IDENTIFIED BY 'login!@#$';

GRANT ALL PRIVILEGES ON login.* TO 'login'@'%' IDENTIFIED BY 'login!@#$';

CREATE database adjustment CHARACTER SET 'utf8mb4';

CREATE USER adjustment@'%' IDENTIFIED BY 'adjustment!@#$';

GRANT ALL PRIVILEGES ON adjustment.* TO 'adjustment'@'%' IDENTIFIED BY 'adjustment!@#$';


FLUSH PRIVILEGES;

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

-- -----------------------------------------------------
-- Schema login
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `login` DEFAULT CHARACTER SET utf8mb4;
USE `login` ;

CREATE TABLE IF NOT EXISTS `login`.`oauth_client_details` (
  client_id VARCHAR(256) PRIMARY KEY,
  resource_ids VARCHAR(256),
  client_secret VARCHAR(256),
  scope VARCHAR(256),
  authorized_grant_types VARCHAR(256),
  web_server_redirect_uri VARCHAR(256),
  authorities VARCHAR(256),
  access_token_validity INTEGER,
  refresh_token_validity INTEGER,
  additional_information VARCHAR(4096),
  autoapprove VARCHAR(256)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE IF NOT EXISTS `login`.`oauth_client_token` (
  token_id VARCHAR(256),
  token BLOB,
  authentication_id VARCHAR(256) PRIMARY KEY,
  user_name VARCHAR(256),
  client_id VARCHAR(256)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `login`.`oauth_access_token` (
  token_id VARCHAR(256),
  token BLOB,
  authentication_id VARCHAR(256) PRIMARY KEY,
  user_name VARCHAR(256),
  client_id VARCHAR(256),
  authentication BLOB,
  refresh_token VARCHAR(256)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `login`.`oauth_refresh_token` (
  token_id VARCHAR(256),
  token BLOB,
  authentication BLOB
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `login`.`oauth_code` (
  code VARCHAR(256),
  authentication BLOB
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `login`.`oauth_approvals` (
	userId VARCHAR(256),
	clientId VARCHAR(256),
	scope VARCHAR(256),
	status VARCHAR(10),
	expiresAt TIMESTAMP,
	lastModifiedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE IF NOT EXISTS `login`.`client_details` (
  appId VARCHAR(256) PRIMARY KEY,
  resourceIds VARCHAR(256),
  appSecret VARCHAR(256),
  scope VARCHAR(256),
  grantTypes VARCHAR(256),
  redirectUrl VARCHAR(256),
  authorities VARCHAR(256),
  access_token_validity INTEGER,
  refresh_token_validity INTEGER,
  additionalInformation VARCHAR(4096),
  autoApproveScopes VARCHAR(256)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `login`.`user` (
  `user_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(25) NOT NULL ,
  `password` VARCHAR(64) NOT NULL ,
  `enabled` TINYINT(1) NOT NULL DEFAULT 1,
  `user_type` VARCHAR(1) NOT NULL,
  `mall_id` BIGINT(20) NOT NULL,
  `password_failed_count` SMALLINT(5) NOT NULL DEFAULT 0,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `user_idx1` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `login`.`user_roles` (
  `user_role_id` INT(11) NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(25) NOT NULL,
  `role` VARCHAR(20) NOT NULL,
  PRIMARY KEY (`user_role_id`),
  UNIQUE KEY `user_roles_idx1` (`role`, `username`),
  KEY `user_roles_idx2` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE IF NOT EXISTS `adjustment``BATCH_JOB_INSTANCE` (
  `JOB_INSTANCE_ID` bigint(20) NOT NULL,
  `VERSION` bigint(20) DEFAULT NULL,
  `JOB_NAME` varchar(100) NOT NULL,
  `JOB_KEY` varchar(32) NOT NULL,
  PRIMARY KEY (`JOB_INSTANCE_ID`),
  UNIQUE KEY `JOB_INST_UN` (`JOB_NAME`,`JOB_KEY`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `adjustment``BATCH_JOB_EXECUTION` (
  `JOB_EXECUTION_ID` bigint(20) NOT NULL,
  `VERSION` bigint(20) DEFAULT NULL,
  `JOB_INSTANCE_ID` bigint(20) NOT NULL,
  `CREATE_TIME` datetime NOT NULL,
  `START_TIME` datetime DEFAULT NULL,
  `END_TIME` datetime DEFAULT NULL,
  `STATUS` varchar(10) DEFAULT NULL,
  `EXIT_CODE` varchar(2500) DEFAULT NULL,
  `EXIT_MESSAGE` varchar(2500) DEFAULT NULL,
  `LAST_UPDATED` datetime DEFAULT NULL,
  `JOB_CONFIGURATION_LOCATION` varchar(2500) DEFAULT NULL,
  PRIMARY KEY (`JOB_EXECUTION_ID`),
  KEY `JOB_INST_EXEC_FK` (`JOB_INSTANCE_ID`),
  CONSTRAINT `JOB_INST_EXEC_FK` FOREIGN KEY (`JOB_INSTANCE_ID`) REFERENCES `BATCH_JOB_INSTANCE` (`JOB_INSTANCE_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE IF NOT EXISTS `adjustment``BATCH_JOB_EXECUTION_CONTEXT` (
  `JOB_EXECUTION_ID` bigint(20) NOT NULL,
  `SHORT_CONTEXT` varchar(2500) NOT NULL,
  `SERIALIZED_CONTEXT` text,
  PRIMARY KEY (`JOB_EXECUTION_ID`),
  CONSTRAINT `JOB_EXEC_CTX_FK` FOREIGN KEY (`JOB_EXECUTION_ID`) REFERENCES `BATCH_JOB_EXECUTION` (`JOB_EXECUTION_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE IF NOT EXISTS `adjustment``BATCH_JOB_EXECUTION_PARAMS` (
  `JOB_EXECUTION_ID` bigint(20) NOT NULL,
  `TYPE_CD` varchar(6) NOT NULL,
  `KEY_NAME` varchar(100) NOT NULL,
  `STRING_VAL` varchar(250) DEFAULT NULL,
  `DATE_VAL` datetime DEFAULT NULL,
  `LONG_VAL` bigint(20) DEFAULT NULL,
  `DOUBLE_VAL` double DEFAULT NULL,
  `IDENTIFYING` char(1) NOT NULL,
  KEY `JOB_EXEC_PARAMS_FK` (`JOB_EXECUTION_ID`),
  CONSTRAINT `JOB_EXEC_PARAMS_FK` FOREIGN KEY (`JOB_EXECUTION_ID`) REFERENCES `BATCH_JOB_EXECUTION` (`JOB_EXECUTION_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `adjustment``BATCH_JOB_EXECUTION_SEQ` (
  `ID` bigint(20) NOT NULL,
  `UNIQUE_KEY` char(1) NOT NULL,
  UNIQUE KEY `UNIQUE_KEY_UN` (`UNIQUE_KEY`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `adjustment``BATCH_JOB_SEQ` (
  `ID` bigint(20) NOT NULL,
  `UNIQUE_KEY` char(1) NOT NULL,
  UNIQUE KEY `UNIQUE_KEY_UN` (`UNIQUE_KEY`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `adjustment``BATCH_STEP_EXECUTION` (
  `STEP_EXECUTION_ID` bigint(20) NOT NULL,
  `VERSION` bigint(20) NOT NULL,
  `STEP_NAME` varchar(100) NOT NULL,
  `JOB_EXECUTION_ID` bigint(20) NOT NULL,
  `START_TIME` datetime NOT NULL,
  `END_TIME` datetime DEFAULT NULL,
  `STATUS` varchar(10) DEFAULT NULL,
  `COMMIT_COUNT` bigint(20) DEFAULT NULL,
  `READ_COUNT` bigint(20) DEFAULT NULL,
  `FILTER_COUNT` bigint(20) DEFAULT NULL,
  `WRITE_COUNT` bigint(20) DEFAULT NULL,
  `READ_SKIP_COUNT` bigint(20) DEFAULT NULL,
  `WRITE_SKIP_COUNT` bigint(20) DEFAULT NULL,
  `PROCESS_SKIP_COUNT` bigint(20) DEFAULT NULL,
  `ROLLBACK_COUNT` bigint(20) DEFAULT NULL,
  `EXIT_CODE` varchar(2500) DEFAULT NULL,
  `EXIT_MESSAGE` varchar(2500) DEFAULT NULL,
  `LAST_UPDATED` datetime DEFAULT NULL,
  PRIMARY KEY (`STEP_EXECUTION_ID`),
  KEY `JOB_EXEC_STEP_FK` (`JOB_EXECUTION_ID`),
  CONSTRAINT `JOB_EXEC_STEP_FK` FOREIGN KEY (`JOB_EXECUTION_ID`) REFERENCES `BATCH_JOB_EXECUTION` (`JOB_EXECUTION_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `adjustment``BATCH_STEP_EXECUTION_CONTEXT` (
  `STEP_EXECUTION_ID` bigint(20) NOT NULL,
  `SHORT_CONTEXT` varchar(2500) NOT NULL,
  `SERIALIZED_CONTEXT` text,
  PRIMARY KEY (`STEP_EXECUTION_ID`),
  CONSTRAINT `STEP_EXEC_CTX_FK` FOREIGN KEY (`STEP_EXECUTION_ID`) REFERENCES `BATCH_STEP_EXECUTION` (`STEP_EXECUTION_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `adjustment``BATCH_STEP_EXECUTION_SEQ` (
  `ID` bigint(20) NOT NULL,
  `UNIQUE_KEY` char(1) NOT NULL,
  UNIQUE KEY `UNIQUE_KEY_UN` (`UNIQUE_KEY`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;




CREATE TABLE IF NOT EXISTS `adjustment`.`mall_service_info` (
  `mall_id` BIGINT(20) NOT NULL,
  `free_service_end_dt` DATETIME NULL, # 무료 상담기간
  `free_cash_avaiable_dt` DATETIME NULL, # 무료 캐쉬 사용기간
  `free_cash` INT(11) NOT NULL DEFAULT 0, # 무료 캐쉬
  `cash` INT(11) NOT NULL DEFAULT 0, # 보유 캐쉬
  `first_payment_at` DATETIME NULL, # 최초 유료 결제일
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`mall_id`),
  KEY `mall_service_info_idx1` (`first_payment_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `adjustment`.`mall_cash_his` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `mall_id` BIGINT(20) NOT NULL,
  `old_cash` INT(11) NOT NULL DEFAULT 0, -- 이전 캐쉬
  `new_cash` INT(11) NOT NULL DEFAULT 0, -- 이후 캐쉬
  `cash_changed` INT(11) NOT NULL DEFAULT 0, -- 캐쉬 변동액
  `is_free` TINYINT(1) NOT NULL,  -- 무료 캐쉬 여부
  `is_charge` TINYINT(1) NOT NULL,  -- 충전 여부
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `mall_cash_his_idx1` (`mall_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `adjustment`.`mall_cdr_his` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `mall_id` BIGINT(20) NOT NULL,
  `mall_user` VARCHAR(128) NOT NULL,
  `session` VARCHAR(128) NOT NULL,
  `created_at` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `mall_cdr_his_idx1` (`mall_id`, `session`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;




CREATE TABLE IF NOT EXISTS `adjustment`.`mall_billing_stat` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `date` VARCHAR(10) NOT NULL,
  `mall_id` BIGINT(20) NOT NULL,
  `payment_credit` INT(11) NOT NULL DEFAULT 0,
  `payment_bankbook` INT(11) NOT NULL DEFAULT 0, 
  `cash_plus_free` INT(11) NOT NULL DEFAULT 0,
  `cash_plus_pay` INT(11) NOT NULL DEFAULT 0,
  `cash_minus_free` INT(11) NOT NULL DEFAULT 0,
  `cash_minus_pay` INT(11) NOT NULL DEFAULT 0,
  `cash_remain_free` INT(11) NOT NULL DEFAULT 0,
  `cash_remain_pay` INT(11) NOT NULL DEFAULT 0,
  `free_cdr_count` INT(11) NOT NULL DEFAULT 0, # Kafka를 통한 무료 cdr count
  `chat_cdr_count` INT(11) NOT NULL DEFAULT 0, # 실제 chatting cdr count
  `is_stat_record` TINYINT(1) NOT NULL DEFAULT 0, # 통계 기록 여부
  PRIMARY KEY (`id`),
  UNIQUE KEY `mall_billing_stat_idx1` (`date`, `mall_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `adjustment`.`sb_users` (
  `ID` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `user_login` VARCHAR(60) NOT NULL,
  `user_pass` VARCHAR(255) NOT NULL,
  `user_email` VARCHAR(100) NOT NULL,
  `user_status` int(11) NOT NULL DEFAULT 1, # 1 : 정상등록 7 : 회원탈퇴 9 : 회원삭제
  `biz_name` VARCHAR(50) NULL,
  `biz_no` VARCHAR(12) NULL,
  `biz_domain` VARCHAR(50) NULL,
  `boss_name` VARCHAR(30) NULL,
  `boss_phone` VARCHAR(15) NULL,
  `sso_user_id` BIGINT(20) NULL,
  `sso_username_markered` VARCHAR(50) NULL,
  `sso_mall_id` BIGINT(20) NULL,
  `custom_data_01` VARCHAR(500) NULL,
  `custom_data_02` VARCHAR(500) NULL,
  `custom_data_03` VARCHAR(500) NULL,
  `custom_data_04` VARCHAR(500) NULL,
  `custom_data_05` VARCHAR(500) NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE IF NOT EXISTS `adjustment`.`pb_paygate_order` (
  `ID`                BIGINT(20)    NOT NULL AUTO_INCREMENT,
  `order_name`        VARCHAR(50)   NOT NULL, #--주문서명
  `status`            VARCHAR(10)   NOT NULL, #--주문상태(paid - 결제완료 ready - 결제대기(입금대기) cancelled - 취소됨 failed - 실패함 wait - 작성중)
  `paymethod`         VARCHAR(10)   NOT NULL, #--결제방법(card - 카드 vbank - 가상계좌 trans - 실시간이체 phone - 핸드폰소액결제)
  `buyer_name`        VARCHAR(20)   NULL,     #--결제자명
  `buyer_phone`       VARCHAR(40)   NULL,     #--결제자명
  `buyer_email`       VARCHAR(70)   NULL,     #--결제자연락처
  `amount`            BIGINT(10)    NOT NULL, #--결제금액
  `order_date`        DATETIME      NOT NULL, #--주문일자
  `paid_date`         DATETIME      NULL,     #--결제완료일자
  `failed_date`       DATETIME      NULL,     #--결제실패일자
  `failed_rmk`        VARCHAR(500)  NULL,
  `cancelled_date`    DATETIME      NULL,     #--결제취소일자
  `cancelled_rmk`     VARCHAR(500)  NULL,     #--결제취소사유
  `cancelled_amount`  BIGINT(10)    NULL,     #--취소금액
  PRIMARY KEY (`ID`),
  KEY `pb_paygate_order_idx1` (`status`, `paid_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `adjustment`.`pb_cash_order` (
  `ID`            BIGINT(20)  NOT NULL AUTO_INCREMENT,
  `user_id`       BIGINT(20)  NOT NULL, #사용자ID
  `service_type`  VARCHAR(5)  NOT NULL, #서비스구분(00001 => 무료체험, 00003 => 유료서비스, 00005 => 요금충전, 00009 => 서비스정지)
  `status`        VARCHAR(5)  NOT NULL, #주문상태(00001 => 대기 (PG결제 전 등, 제출 전 상태), 00003 => 신청요청, 00005 => 승인, 90001 => 취소됨, 90003 => 반려)
  `pay_type`      VARCHAR(5)  NULL,     #결제구분(NULL => 해당없음, 00001 => 무통장입금, 00003 => PG결제)
  `pg_order_id`   VARCHAR(20) NULL,     #PG주문ID(무통장입금일 때 NULL)
  `amount`        BIGINT(15)  NULL,     #결제금액 
  `cash`          BIGINT(15)  NULL,     #충전캐쉬 
  `applied_date`  DATETIME    NULL,     #신청일자 
  `confirmed_date`DATETIME    NULL,     #승인일자 
  `rejected_date` DATETIME    NULL,     #반려일자 
  `paid_date`     DATETIME    NULL,     #결제일자 
  `use_srt_date`  DATETIME    NULL,     #사용시작일자
  `use_end_date`  DATETIME    NULL,     #사용종료일자
  `apply_name`    VARCHAR(50) NULL,
  `apply_phone`   VARCHAR(20) NULL,
  PRIMARY KEY (`ID`),
  KEY `pb_cash_order_idx1` (`service_type`, `status`, `paid_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;