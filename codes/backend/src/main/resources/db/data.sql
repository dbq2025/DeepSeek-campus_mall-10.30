-- ============================================================
-- 校园电商平台 · 本地演示种子数据 v1.0（2026-10-31）
-- 仅用于本地演示库 campus_mall，禁止在生产执行。
-- 幂等：固定主键 + ON DUPLICATE KEY UPDATE，可重复执行。
-- 说明：不复位 sales/stock 之外的业务数据；账号密码统一为 123456（BCrypt）。
-- 执行：mysql --default-character-set=utf8mb4 -uroot -p1234 < data.sql
--      （Windows 客户端默认 gbk，不加该参数中文会被截断报 Data too long）
-- ============================================================
USE `campus_mall`;

-- ---------- 账号 ----------
INSERT INTO `user` (`id`, `username`, `password`, `nickname`, `email`, `phone`, `role`, `status`)
VALUES
  (1, 'admin', '$2a$10$ndH/ZF742PfrtpZApizqKOvW1.nrLVKe0kqgFZ49wZSGOWAnFLd6.', '平台管理员', 'admin@campus.edu', '13800000000', 'ADMIN', 1),
  (2, 'test',  '$2a$10$ndH/ZF742PfrtpZApizqKOvW1.nrLVKe0kqgFZ49wZSGOWAnFLd6.', '小明同学', 'test@campus.edu',  '13900000000', 'USER',  1),
  (3, 'lisi',  '$2a$10$ndH/ZF742PfrtpZApizqKOvW1.nrLVKe0kqgFZ49wZSGOWAnFLd6.', '李四',       'lisi@campus.edu',  '13700000000', 'USER',  1)
ON DUPLICATE KEY UPDATE `password` = VALUES(`password`), `nickname` = VALUES(`nickname`),
                        `role` = VALUES(`role`), `status` = VALUES(`status`);

-- ---------- 商品类型 ----------
INSERT INTO `category` (`id`, `name`, `sort`)
VALUES
  (1001, '数码电子', 1),
  (1002, '图书教材', 2),
  (1003, '生活用品', 3),
  (1004, '运动户外', 4),
  (1005, '零食饮品', 5)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`), `sort` = VALUES(`sort`);

-- ---------- 商品 ----------
INSERT INTO `product` (`id`, `category_id`, `name`, `subtitle`, `main_image`, `price`, `stock`,
                       `spec_json`, `detail`, `status`, `sales`)
VALUES
  (2001, 1001, '真无线蓝牙耳机', '主动降噪 · 续航 30 小时', '/files/p1.svg', 199.00, 120,
   '[{"name":"颜色","values":["云雾白","曜石黑"]}]',
   '蓝牙 5.3，入耳式主动降噪，单次续航 8 小时，配合充电仓可达 30 小时。支持双设备连接。', 1, 86),
  (2002, 1001, '机械键盘 87 键', '热插拔轴体 · 三模连接', '/files/p2.svg', 349.00, 60,
   '[{"name":"轴体","values":["青轴","红轴","茶轴"]}]',
   '支持有线、2.4G、蓝牙三模，全键热插拔，Gasket 结构，附赠拔键器。', 1, 54),
  (2003, 1002, '数据结构与算法分析', 'C 语言描述 · 第 2 版', '/files/p3.svg', 59.80, 200,
   NULL,
   '经典教材，覆盖线性表、树、图、排序与查找，配套课后习题解析。', 1, 210),
  (2004, 1002, '计算机网络自顶向下方法', '第 7 版 · 英文影印', '/files/p4.svg', 89.00, 150,
   NULL,
   '从应用层自顶向下讲解网络原理，适合课程设计与考研复习。', 1, 132),
  (2005, 1003, '便携保温杯 500ml', '316 不锈钢 · 24 小时保温', '/files/p5.svg', 79.00, 300,
   '[{"name":"颜色","values":["雾霾蓝","奶白","墨绿"]}]',
   '316 食品级不锈钢内胆，弹盖设计，一键开合，容量 500ml。', 1, 168),
  (2006, 1003, '桌面收纳盒', '三层抽屉 · 磨砂质感', '/files/p6.svg', 45.00, 180,
   NULL,
   '宿舍桌面收纳，三层抽屉可拆分，承重 5kg。', 1, 73),
  (2007, 1004, '瑜伽垫 8mm', '加厚防滑 · 附绑带', '/files/p7.svg', 69.00, 90,
   '[{"name":"厚度","values":["6mm","8mm","10mm"]}]',
   'TPE 环保材质，双面防滑纹理，附赠收纳绑带。', 1, 41),
  (2008, 1004, '跳绳计数款', '智能计数 · 可调长度', '/files/p8.svg', 29.90, 260,
   NULL,
   '内置计数芯片，绳长 3 米可调，适合体测训练。', 1, 95),
  (2009, 1005, '坚果每日礼盒', '7 种混合 · 30 小包', '/files/p1.svg', 99.00, 140,
   NULL,
   '每日一包，独立包装，含巴旦木、腰果、核桃仁等 7 种坚果。', 1, 121),
  (2010, 1005, '冷萃咖啡液', '0 糖 0 脂 · 10 条装', '/files/p2.svg', 49.90, 220,
   '[{"name":"口味","values":["经典","椰乳","生椰拿铁"]}]',
   '冷萃工艺，冷热水皆可冲调，单条 20ml。', 1, 187),
  (2011, 1001, '无线鼠标', '静音按键 · 无线双模', '/files/p3.svg', 89.00, 160,
   '[{"name":"颜色","values":["白","灰"]}]',
   '静音微动，1600DPI 可调，一节电池续航 12 个月。', 1, 64),
  (2012, 1002, '操作系统概念', '第九版 · 恐龙书', '/files/p4.svg', 108.00, 0,
   NULL,
   '操作系统经典教材，进程、内存、文件系统与并发控制讲解详尽。', 0, 45)
ON DUPLICATE KEY UPDATE
  `category_id` = VALUES(`category_id`),
  `name` = VALUES(`name`),
  `subtitle` = VALUES(`subtitle`),
  `main_image` = VALUES(`main_image`),
  `price` = VALUES(`price`),
  `spec_json` = VALUES(`spec_json`),
  `detail` = VALUES(`detail`),
  `status` = VALUES(`status`);
-- 注意：故意不复位 stock / sales，避免每次执行把演示中的库存与销量清零。
