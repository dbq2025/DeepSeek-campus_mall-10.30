-- ============================================================
-- 校园电商平台 · 数据库初始化脚本 v1.0
-- 引擎 MySQL 8.0.34  字符集 utf8mb4
-- 说明：本脚本是 design/20-领域模型与数据库.md 的可执行副本
--       改表先改文档与契约，再改这里，禁止直接改库
-- ============================================================

CREATE DATABASE IF NOT EXISTS `campus_mall`
  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `campus_mall`;

-- ---------- 用户 ----------
CREATE TABLE IF NOT EXISTS `user` (
  `id`          bigint       NOT NULL COMMENT '主键，雪花ID',
  `username`    varchar(50)  NOT NULL COMMENT '登录名',
  `password`    varchar(100) NOT NULL COMMENT 'BCrypt 密文',
  `nickname`    varchar(50)  DEFAULT NULL COMMENT '昵称',
  `email`       varchar(100) DEFAULT NULL COMMENT '邮箱',
  `phone`       varchar(20)  DEFAULT NULL COMMENT '手机号',
  `avatar`      varchar(255) DEFAULT NULL COMMENT '头像相对路径',
  `role`        varchar(20)  NOT NULL DEFAULT 'USER' COMMENT 'USER/ADMIN',
  `status`      tinyint      NOT NULL DEFAULT 1 COMMENT '1正常 0禁用',
  `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表（含管理员）';

-- ---------- 商品类型 ----------
CREATE TABLE IF NOT EXISTS `category` (
  `id`          bigint      NOT NULL,
  `name`        varchar(50) NOT NULL COMMENT '类型名称',
  `sort`        int         NOT NULL DEFAULT 0 COMMENT '排序，越小越前',
  `create_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品类型表';

-- ---------- 商品 ----------
CREATE TABLE IF NOT EXISTS `product` (
  `id`          bigint        NOT NULL,
  `category_id` bigint        NOT NULL COMMENT '所属类型',
  `name`        varchar(100)  NOT NULL COMMENT '商品名',
  `subtitle`    varchar(200)  DEFAULT NULL COMMENT '副标题/卖点',
  `main_image`  varchar(255)  DEFAULT NULL COMMENT '主图相对路径',
  `price`       decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '售价',
  `stock`       int           NOT NULL DEFAULT 0 COMMENT '库存，单维',
  `spec_json`   varchar(500)  DEFAULT NULL COMMENT '规格定义 JSON，空=无规格',
  `detail`      text          COMMENT '详情富文本',
  `status`      tinyint       NOT NULL DEFAULT 1 COMMENT '1上架 0下架',
  `sales`       int           NOT NULL DEFAULT 0 COMMENT '累计销量（统计用）',
  `deleted`     tinyint       NOT NULL DEFAULT 0 COMMENT '逻辑删除 0/1',
  `create_time` datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表（不做真SKU，规格仅展示+快照）';

-- ---------- 购物车 ----------
CREATE TABLE IF NOT EXISTS `cart` (
  `id`          bigint       NOT NULL,
  `user_id`     bigint       NOT NULL,
  `product_id`  bigint       NOT NULL,
  `quantity`    int          NOT NULL DEFAULT 1 COMMENT '数量',
  `spec_text`   varchar(100) NOT NULL DEFAULT '' COMMENT '规格快照，如 颜色:黑;尺码:M',
  `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_product_spec` (`user_id`,`product_id`,`spec_text`),
  KEY `idx_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- ---------- 订单 ----------
CREATE TABLE IF NOT EXISTS `orders` (
  `id`               bigint        NOT NULL,
  `order_no`         varchar(32)   NOT NULL COMMENT '业务单号',
  `user_id`          bigint        NOT NULL,
  `total_amount`     decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '商品总额',
  `pay_amount`       decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '实付金额',
  `status`           tinyint       NOT NULL DEFAULT 0 COMMENT '0待付款 1待发货 2待收货 3已完成 4已取消',
  `receiver_name`    varchar(50)   NOT NULL COMMENT '收货人（快照）',
  `receiver_phone`   varchar(20)   NOT NULL COMMENT '电话（快照）',
  `receiver_address` varchar(255)  NOT NULL COMMENT '地址（快照）',
  `remark`           varchar(200)  DEFAULT NULL COMMENT '买家留言',
  `tracking_no`      varchar(50)   DEFAULT NULL COMMENT '发货单号',
  `pay_time`         datetime      DEFAULT NULL,
  `ship_time`        datetime      DEFAULT NULL,
  `finish_time`      datetime      DEFAULT NULL,
  `cancel_time`      datetime      DEFAULT NULL,
  `cancel_reason`    varchar(50)   DEFAULT NULL,
  `deleted`          tinyint       NOT NULL DEFAULT 0,
  `create_time`      datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`      datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user` (`user_id`),
  KEY `idx_status_create` (`status`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单主表（状态机 see design/01）';

-- ---------- 订单明细 ----------
CREATE TABLE IF NOT EXISTS `order_item` (
  `id`            bigint        NOT NULL,
  `order_id`      bigint        NOT NULL,
  `product_id`    bigint        NOT NULL,
  `product_name`  varchar(100)  NOT NULL COMMENT '下单时商品名（快照）',
  `product_image` varchar(255)  DEFAULT NULL COMMENT '下单时主图（快照）',
  `price`         decimal(10,2) NOT NULL COMMENT '下单时单价（快照）',
  `quantity`      int           NOT NULL,
  `spec_text`     varchar(100)  NOT NULL DEFAULT '' COMMENT '规格快照',
  `total_amount`  decimal(10,2) NOT NULL COMMENT '行小计 = price*quantity',
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`),
  KEY `idx_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表（含价格/名称/图片快照）';

-- ---------- 收藏 ----------
CREATE TABLE IF NOT EXISTS `favorite` (
  `id`          bigint   NOT NULL,
  `user_id`     bigint   NOT NULL,
  `product_id`  bigint   NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_product` (`user_id`,`product_id`),
  KEY `idx_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表（多对多关系表）';
