-- ============================================================
-- 酒店管理系统 - MySQL 建表脚本
-- 严格按照数据字典设计，包含主键、外键、唯一约束、非空、默认值
-- ============================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS hotel_management
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE hotel_management;

-- ============================================================
-- 参照完整性设计原则
-- 1. 主数据（客户、用户、房间、房型）默认不级联删除，避免破坏业务历史。
-- 2. 明细数据（预订明细、入住宾客）依赖主表存在，可随主表级联删除。
-- 3. 关键业务状态由触发器联动维护，减少跨表状态不一致。
-- ============================================================

-- ============================================================
-- 1. 用户表 Users
-- 对应数据字典 D1
-- ============================================================
CREATE TABLE `users` (
    `user_id` INT NOT NULL AUTO_INCREMENT COMMENT '用户编号，主键',
    `username` VARCHAR(50) NOT NULL COMMENT '登录用户名',
    `password_hash` VARCHAR(255) NOT NULL COMMENT '密码哈希值（加盐），不可逆存储',
    `role` VARCHAR(20) NOT NULL COMMENT '用户角色：admin-管理员，frontdesk-前台操作员',
    `permissions` VARCHAR(500) DEFAULT NULL COMMENT '权限字段，JSON或逗号分隔的权限编码',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `last_login` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    PRIMARY KEY (`user_id`),
    UNIQUE KEY `uk_username` (`username`),
    CONSTRAINT `ck_user_role` CHECK (`role` IN ('admin', 'frontdesk'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================================
-- 2. 客户信息表 Customers
-- 对应数据字典 D2
-- ============================================================
CREATE TABLE `customers` (
    `customer_id` INT NOT NULL AUTO_INCREMENT COMMENT '客户编号，主键',
    `id_number` VARCHAR(18) NOT NULL COMMENT '身份证号，18位，末位可为X',
    `name` VARCHAR(50) NOT NULL COMMENT '客户姓名',
    `address` VARCHAR(200) DEFAULT NULL COMMENT '联系地址',
    `phone` VARCHAR(20) NOT NULL COMMENT '联系电话',
    `membership_level` VARCHAR(20) NOT NULL DEFAULT '普通' COMMENT '会员等级：普通/VIP/金卡',
    `points` INT NOT NULL DEFAULT 0 COMMENT '会员积分，非负整数',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT NULL COMMENT '最后更新时间',
    PRIMARY KEY (`customer_id`),
    UNIQUE KEY `uk_id_number` (`id_number`),
    KEY `idx_phone` (`phone`),
    KEY `idx_name` (`name`),
    CONSTRAINT `ck_customer_membership_level` CHECK (`membership_level` IN ('普通', 'VIP', '贵宾', '金卡')),
    CONSTRAINT `ck_customer_points` CHECK (`points` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户信息表';

-- ============================================================
-- 3. 客房类型表 RoomTypes
-- 对应数据字典 D3
-- ============================================================
CREATE TABLE `room_types` (
    `type_id` INT NOT NULL AUTO_INCREMENT COMMENT '类型编号，主键',
    `type_name` VARCHAR(30) NOT NULL COMMENT '类型名称，如单人间/双人间/套房',
    `base_price` DECIMAL(10,2) NOT NULL COMMENT '基础价格（元/晚），必须大于0',
    `capacity` INT NOT NULL DEFAULT 1 COMMENT '最大入住人数',
    `description` VARCHAR(200) DEFAULT NULL COMMENT '类型描述',
    PRIMARY KEY (`type_id`),
    UNIQUE KEY `uk_type_name` (`type_name`),
    CONSTRAINT `ck_room_type_base_price` CHECK (`base_price` > 0),
    CONSTRAINT `ck_room_type_capacity` CHECK (`capacity` > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客房类型表';

-- ============================================================
-- 4. 客房表 Rooms
-- 对应数据字典 D4
-- ============================================================
CREATE TABLE `rooms` (
    `room_id` INT NOT NULL AUTO_INCREMENT COMMENT '房间编号，主键',
    `room_number` VARCHAR(10) NOT NULL COMMENT '房间号，如101、202、A301',
    `type_id` INT NOT NULL COMMENT '所属类型编号，外键引用room_types.type_id',
    `status` VARCHAR(10) NOT NULL DEFAULT '空闲' COMMENT '房间状态：空闲/占用/清洁中/维修中',
    `floor` VARCHAR(10) DEFAULT NULL COMMENT '所在楼层',
    `remark` VARCHAR(200) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`room_id`),
    UNIQUE KEY `uk_room_number` (`room_number`),
    KEY `idx_type_id` (`type_id`),
    KEY `idx_status` (`status`),
    CONSTRAINT `fk_room_type` FOREIGN KEY (`type_id`) REFERENCES `room_types` (`type_id`)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT `ck_room_status` CHECK (`status` IN ('空闲', '占用', '清洁中', '维修中'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客房表';

-- ============================================================
-- 5. 预订记录表 Reservations
-- 对应数据字典 D5
-- ============================================================
CREATE TABLE `reservations` (
    `reservation_id` INT NOT NULL AUTO_INCREMENT COMMENT '预订编号，主键',
    `customer_id` INT NOT NULL COMMENT '主要联系人客户编号，外键引用customers.customer_id',
    `created_by` INT NOT NULL COMMENT '操作前台用户编号，外键引用users.user_id',
    `expected_checkin` DATETIME NOT NULL COMMENT '预计入住时间',
    `expected_checkout` DATETIME NOT NULL COMMENT '预计离开时间，必须大于预计入住时间',
    `guest_count` INT NOT NULL DEFAULT 1 COMMENT '总入住人数',
    `status` VARCHAR(10) NOT NULL DEFAULT '已确认' COMMENT '预订状态：已确认/已入住/已取消/已完成',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '预订创建时间',
    `remark` VARCHAR(200) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`reservation_id`),
    KEY `idx_customer_id` (`customer_id`),
    KEY `idx_created_by` (`created_by`),
    KEY `idx_status` (`status`),
    KEY `idx_expected_checkin` (`expected_checkin`),
    CONSTRAINT `fk_reservation_customer` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`customer_id`)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT `fk_reservation_user` FOREIGN KEY (`created_by`) REFERENCES `users` (`user_id`)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT `ck_reservation_time` CHECK (`expected_checkout` > `expected_checkin`),
    CONSTRAINT `ck_reservation_guest_count` CHECK (`guest_count` > 0),
    CONSTRAINT `ck_reservation_status` CHECK (`status` IN ('已确认', '已入住', '已取消', '已完成'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预订记录表';

-- ============================================================
-- 6. 预订需求明细表 ReservationDetails
-- 对应数据字典 D5A
-- ============================================================
CREATE TABLE `reservation_details` (
    `detail_id` INT NOT NULL AUTO_INCREMENT COMMENT '明细编号，主键',
    `reservation_id` INT NOT NULL COMMENT '所属预订编号，外键引用reservations.reservation_id',
    `type_id` INT NOT NULL COMMENT '需求房间类型编号，外键引用room_types.type_id',
    `room_count` INT NOT NULL DEFAULT 1 COMMENT '需求间数，必须大于0',
    PRIMARY KEY (`detail_id`),
    UNIQUE KEY `uk_reservation_type` (`reservation_id`, `type_id`),
    KEY `idx_reservation_id` (`reservation_id`),
    KEY `idx_type_id` (`type_id`),
    CONSTRAINT `fk_detail_reservation` FOREIGN KEY (`reservation_id`) REFERENCES `reservations` (`reservation_id`)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT `fk_detail_room_type` FOREIGN KEY (`type_id`) REFERENCES `room_types` (`type_id`)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT `ck_reservation_detail_room_count` CHECK (`room_count` > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预订需求明细表';

-- ============================================================
-- 7. 入住登记表 Checkins
-- 对应数据字典 D6
-- ============================================================
CREATE TABLE `checkins` (
    `checkin_id` INT NOT NULL AUTO_INCREMENT COMMENT '登记编号，主键',
    `reservation_id` INT DEFAULT NULL COMMENT '关联预订编号，可空，外键引用reservations.reservation_id',
    `room_id` INT NOT NULL COMMENT '分配房间编号，外键引用rooms.room_id',
    `checkin_time` DATETIME NOT NULL COMMENT '入住时间',
    `checkout_time` DATETIME DEFAULT NULL COMMENT '实际退房时间，结账时填入',
    `status` VARCHAR(10) NOT NULL DEFAULT '入住中' COMMENT '入住状态：入住中/已退房/已换房',
    PRIMARY KEY (`checkin_id`),
    KEY `idx_reservation_id` (`reservation_id`),
    KEY `idx_room_id` (`room_id`),
    KEY `idx_status` (`status`),
    CONSTRAINT `fk_checkin_reservation` FOREIGN KEY (`reservation_id`) REFERENCES `reservations` (`reservation_id`)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT `fk_checkin_room` FOREIGN KEY (`room_id`) REFERENCES `rooms` (`room_id`)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT `ck_checkin_time` CHECK (`checkout_time` IS NULL OR `checkout_time` >= `checkin_time`),
    CONSTRAINT `ck_checkin_status` CHECK (`status` IN ('入住中', '已退房', '已换房'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='入住登记表';

-- ============================================================
-- 8. 入住宾客明细表 CheckinGuests
-- 对应数据字典 D6A
-- ============================================================
CREATE TABLE `checkin_guests` (
    `guest_id` INT NOT NULL AUTO_INCREMENT COMMENT '宾客明细编号，主键',
    `checkin_id` INT NOT NULL COMMENT '所属入住登记编号，外键引用checkins.checkin_id',
    `customer_id` INT NOT NULL COMMENT '宾客客户编号，外键引用customers.customer_id',
    `is_primary` TINYINT NOT NULL DEFAULT 0 COMMENT '是否主要入住人：0-否，1-是',
    PRIMARY KEY (`guest_id`),
    UNIQUE KEY `uk_checkin_customer` (`checkin_id`, `customer_id`),
    KEY `idx_checkin_id` (`checkin_id`),
    KEY `idx_customer_id` (`customer_id`),
    CONSTRAINT `fk_guest_checkin` FOREIGN KEY (`checkin_id`) REFERENCES `checkins` (`checkin_id`)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT `fk_guest_customer` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`customer_id`)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT `ck_guest_is_primary` CHECK (`is_primary` IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='入住宾客明细表';

-- ============================================================
-- 9. 结账记录表 Bills
-- 对应数据字典 D7
-- ============================================================
CREATE TABLE `bills` (
    `bill_id` INT NOT NULL AUTO_INCREMENT COMMENT '账单编号，主键',
    `checkin_id` INT NOT NULL COMMENT '关联入住登记编号，外键引用checkins.checkin_id，一条入住记录最多一条账单',
    `total_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '费用合计',
    `payment_time` DATETIME NOT NULL COMMENT '结账时间',
    `details` TEXT DEFAULT NULL COMMENT '费用明细，JSON或文本格式',
    `created_by` INT NOT NULL COMMENT '操作前台用户编号，外键引用users.user_id',
    PRIMARY KEY (`bill_id`),
    UNIQUE KEY `uk_checkin_id` (`checkin_id`),
    KEY `idx_created_by` (`created_by`),
    CONSTRAINT `fk_bill_checkin` FOREIGN KEY (`checkin_id`) REFERENCES `checkins` (`checkin_id`)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT `fk_bill_user` FOREIGN KEY (`created_by`) REFERENCES `users` (`user_id`)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT `ck_bill_total_amount` CHECK (`total_amount` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='结账记录表';

-- ============================================================
-- 10. 换房记录表 RoomChanges
-- 对应数据字典 D8
-- ============================================================
CREATE TABLE `room_changes` (
    `change_id` INT NOT NULL AUTO_INCREMENT COMMENT '换房编号，主键',
    `checkin_id` INT NOT NULL COMMENT '关联入住登记编号，外键引用checkins.checkin_id',
    `old_room_id` INT NOT NULL COMMENT '原房间编号，外键引用rooms.room_id',
    `new_room_id` INT NOT NULL COMMENT '新房间编号，外键引用rooms.room_id',
    `change_time` DATETIME NOT NULL COMMENT '换房时间',
    `reason` VARCHAR(200) DEFAULT NULL COMMENT '换房原因',
    PRIMARY KEY (`change_id`),
    KEY `idx_checkin_id` (`checkin_id`),
    KEY `idx_old_room_id` (`old_room_id`),
    KEY `idx_new_room_id` (`new_room_id`),
    CONSTRAINT `fk_change_checkin` FOREIGN KEY (`checkin_id`) REFERENCES `checkins` (`checkin_id`)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT `fk_change_old_room` FOREIGN KEY (`old_room_id`) REFERENCES `rooms` (`room_id`)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT `fk_change_new_room` FOREIGN KEY (`new_room_id`) REFERENCES `rooms` (`room_id`)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='换房记录表';

-- ============================================================
-- 触发器：删除保护、状态联动、关键属性修改影响
-- ============================================================
DELIMITER $$

-- 客户已有业务历史时不建议物理删除；正在入住时更必须禁止删除。
CREATE TRIGGER `trg_customer_before_delete`
BEFORE DELETE ON `customers`
FOR EACH ROW
BEGIN
    IF EXISTS (
        SELECT 1
        FROM `checkin_guests` cg
        JOIN `checkins` c ON c.`checkin_id` = cg.`checkin_id`
        WHERE cg.`customer_id` = OLD.`customer_id`
          AND c.`status` = '入住中'
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = '客户当前正在入住，不能删除';
    END IF;

    IF EXISTS (SELECT 1 FROM `reservations` WHERE `customer_id` = OLD.`customer_id`)
       OR EXISTS (SELECT 1 FROM `checkin_guests` WHERE `customer_id` = OLD.`customer_id`) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = '客户已有预订或入住历史，建议改为软删除/停用，不能物理删除';
    END IF;
END$$

-- 用户产生过业务记录后应保留审计链路。
CREATE TRIGGER `trg_user_before_delete`
BEFORE DELETE ON `users`
FOR EACH ROW
BEGIN
    IF EXISTS (SELECT 1 FROM `reservations` WHERE `created_by` = OLD.`user_id`)
       OR EXISTS (SELECT 1 FROM `bills` WHERE `created_by` = OLD.`user_id`) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = '用户已有业务操作记录，不能物理删除';
    END IF;
END$$

-- 房型被房间或预订明细引用时不能删除；降低容量不能影响当前正在入住的客人。
CREATE TRIGGER `trg_room_type_before_delete`
BEFORE DELETE ON `room_types`
FOR EACH ROW
BEGIN
    IF EXISTS (SELECT 1 FROM `rooms` WHERE `type_id` = OLD.`type_id`)
       OR EXISTS (SELECT 1 FROM `reservation_details` WHERE `type_id` = OLD.`type_id`) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = '房型已被房间或预订明细引用，不能删除';
    END IF;
END$$

CREATE TRIGGER `trg_room_type_before_update`
BEFORE UPDATE ON `room_types`
FOR EACH ROW
BEGIN
    IF NEW.`capacity` < OLD.`capacity`
       AND EXISTS (
           SELECT 1
           FROM `checkins` c
           JOIN `rooms` r ON r.`room_id` = c.`room_id`
           JOIN `checkin_guests` cg ON cg.`checkin_id` = c.`checkin_id`
           WHERE r.`type_id` = OLD.`type_id`
             AND c.`status` = '入住中'
           GROUP BY c.`checkin_id`
           HAVING COUNT(*) > NEW.`capacity`
       ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = '新容量小于当前入住人数，不能修改房型容量';
    END IF;
END$$

-- 房间已有入住或换房历史时不物理删除。
CREATE TRIGGER `trg_room_before_delete`
BEFORE DELETE ON `rooms`
FOR EACH ROW
BEGIN
    IF EXISTS (SELECT 1 FROM `checkins` WHERE `room_id` = OLD.`room_id` AND `status` = '入住中') THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = '房间当前正在入住，不能删除';
    END IF;

    IF EXISTS (SELECT 1 FROM `checkins` WHERE `room_id` = OLD.`room_id`)
       OR EXISTS (SELECT 1 FROM `room_changes` WHERE `old_room_id` = OLD.`room_id` OR `new_room_id` = OLD.`room_id`) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = '房间已有入住或换房历史，不能物理删除';
    END IF;
END$$

-- 已入住或已完成的预订不能删除；关键时间字段不能在入住后继续修改。
CREATE TRIGGER `trg_reservation_before_delete`
BEFORE DELETE ON `reservations`
FOR EACH ROW
BEGIN
    IF OLD.`status` IN ('已入住', '已完成')
       OR EXISTS (SELECT 1 FROM `checkins` WHERE `reservation_id` = OLD.`reservation_id`) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = '预订已产生入住记录，不能删除';
    END IF;
END$$

CREATE TRIGGER `trg_reservation_before_update`
BEFORE UPDATE ON `reservations`
FOR EACH ROW
BEGIN
    IF OLD.`status` IN ('已入住', '已完成')
       AND (NEW.`expected_checkin` <> OLD.`expected_checkin`
            OR NEW.`expected_checkout` <> OLD.`expected_checkout`
            OR NEW.`guest_count` <> OLD.`guest_count`) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = '预订已入住或完成，不能修改预计时间和人数';
    END IF;

    IF OLD.`status` = '已完成' AND NEW.`status` <> '已完成' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = '已完成预订不能回退状态';
    END IF;
END$$

CREATE TRIGGER `trg_checkin_before_update`
BEFORE UPDATE ON `checkins`
FOR EACH ROW
BEGIN
    IF OLD.`status` = '已退房' AND NEW.`status` <> '已退房' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = '已退房入住记录不能恢复为其他状态';
    END IF;

    IF NEW.`room_id` <> OLD.`room_id` THEN
        IF (
            SELECT COUNT(*)
            FROM `checkin_guests`
            WHERE `checkin_id` = OLD.`checkin_id`
        ) > (
            SELECT rt.`capacity`
            FROM `rooms` r
            JOIN `room_types` rt ON rt.`type_id` = r.`type_id`
            WHERE r.`room_id` = NEW.`room_id`
        ) THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '新房间容量不足，不能换房';
        END IF;
    END IF;

    IF NEW.`status` = '已退房' AND NEW.`checkout_time` IS NULL THEN
        SET NEW.`checkout_time` = CURRENT_TIMESTAMP;
    END IF;
END$$

CREATE TRIGGER `trg_checkin_before_delete`
BEFORE DELETE ON `checkins`
FOR EACH ROW
BEGIN
    IF OLD.`status` = '入住中' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = '入住中记录不能删除，请先退房';
    END IF;

    IF EXISTS (SELECT 1 FROM `bills` WHERE `checkin_id` = OLD.`checkin_id`)
       OR EXISTS (SELECT 1 FROM `room_changes` WHERE `checkin_id` = OLD.`checkin_id`) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = '入住记录已有账单或换房历史，不能删除';
    END IF;
END$$

-- 入住宾客变更会影响房间容量约束和主要入住人唯一性。
CREATE TRIGGER `trg_checkin_guest_before_insert`
BEFORE INSERT ON `checkin_guests`
FOR EACH ROW
BEGIN
    IF NEW.`is_primary` = 1
       AND EXISTS (
           SELECT 1
           FROM `checkin_guests`
           WHERE `checkin_id` = NEW.`checkin_id`
             AND `is_primary` = 1
       ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = '同一入住记录只能有一个主要入住人';
    END IF;

    IF (
        SELECT COUNT(*) + 1
        FROM `checkin_guests`
        WHERE `checkin_id` = NEW.`checkin_id`
    ) > (
        SELECT rt.`capacity`
        FROM `checkins` c
        JOIN `rooms` r ON r.`room_id` = c.`room_id`
        JOIN `room_types` rt ON rt.`type_id` = r.`type_id`
        WHERE c.`checkin_id` = NEW.`checkin_id`
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = '入住人数超过房间容量';
    END IF;
END$$

CREATE TRIGGER `trg_checkin_guest_before_update`
BEFORE UPDATE ON `checkin_guests`
FOR EACH ROW
BEGIN
    IF NEW.`is_primary` = 1
       AND EXISTS (
           SELECT 1
           FROM `checkin_guests`
           WHERE `checkin_id` = NEW.`checkin_id`
             AND `guest_id` <> OLD.`guest_id`
             AND `is_primary` = 1
       ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = '同一入住记录只能有一个主要入住人';
    END IF;
END$$

-- 账单生成后自动完成退房，并保护账单不可物理删除。
CREATE TRIGGER `trg_bill_before_insert`
BEFORE INSERT ON `bills`
FOR EACH ROW
BEGIN
    IF (SELECT `status` FROM `checkins` WHERE `checkin_id` = NEW.`checkin_id`) NOT IN ('入住中', '已换房') THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = '只有未结账入住记录才能生成账单';
    END IF;
END$$

CREATE TRIGGER `trg_bill_before_update`
BEFORE UPDATE ON `bills`
FOR EACH ROW
BEGIN
    IF NEW.`checkin_id` <> OLD.`checkin_id` THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = '账单不能改绑入住记录';
    END IF;
END$$

CREATE TRIGGER `trg_bill_before_delete`
BEFORE DELETE ON `bills`
FOR EACH ROW
BEGIN
    SIGNAL SQLSTATE '45000'
    SET MESSAGE_TEXT = '账单属于财务历史，不能物理删除';
END$$

CREATE TRIGGER `trg_room_change_before_delete`
BEFORE DELETE ON `room_changes`
FOR EACH ROW
BEGIN
    SIGNAL SQLSTATE '45000'
    SET MESSAGE_TEXT = '换房记录属于业务历史，不能物理删除';
END$$

DELIMITER ;

-- ============================================================
-- 初始化数据：默认管理员与前台账号
-- 密码均使用 BCrypt 加盐哈希值
-- ============================================================
INSERT INTO `users` (`username`, `password_hash`, `role`, `permissions`, `created_at`)
VALUES
    ('admin', '$2b$12$yyNBUyxFJJvcJfXMlM8YuOLEeRp8cIVxkyBdOsjpNZK3zdsdDGjpe', 'admin', '*', NOW()),
    ('front1', '$2b$12$ts7UDOjLcNdcHp86G/BxWeqJ7u84aw1rgXZcmllNF0i7okixgCqka', 'frontdesk',
     '["customer_add","customer_query","reservation_manage","checkin_manage","room_change","billing"]', NOW());
-- 默认管理员账号: admin / admin123
-- 默认前台账号: front1 / 123456
-- 首次登录后请立即修改密码

-- ============================================================
-- 初始化测试数据：用于验收演示
-- 说明：保留空间给后续前台手工新增的“验收客户/验收房型/验收房间”
-- ============================================================
INSERT INTO `customers` (`id_number`, `name`, `address`, `phone`, `membership_level`, `points`, `created_at`, `updated_at`)
VALUES
    ('110101199001010011', '张三', '北京市朝阳区', '13800138011', '普通', 0, NOW(), NOW()),
    ('110101199002020022', '李四', '上海市浦东新区', '13800138012', 'VIP', 1200, NOW(), NOW()),
    ('110101199003030033', '王五', '广州市天河区', '13800138013', '贵宾', 5600, NOW(), NOW()),
    ('110101199004040044', '赵六', '深圳市南山区', '13800138014', '普通', 200, NOW(), NOW());

INSERT INTO `room_types` (`type_name`, `base_price`, `capacity`, `description`)
VALUES
    ('标准单人间', 280.00, 1, '基础单人客房'),
    ('标准双人间', 420.00, 2, '基础双人客房'),
    ('行政套房', 680.00, 3, '用于高端接待的套房');

INSERT INTO `rooms` (`room_number`, `type_id`, `status`, `floor`, `remark`)
VALUES
    ('1001', 1, '空闲', '1楼', ''),
    ('1002', 1, '空闲', '1楼', ''),
    ('2001', 2, '空闲', '2楼', ''),
    ('2002', 2, '空闲', '2楼', ''),
    ('3001', 3, '空闲', '3楼', ''),
    ('3002', 3, '维修中', '3楼', '空调检修');

INSERT INTO `reservations` (`customer_id`, `created_by`, `expected_checkin`, `expected_checkout`, `guest_count`, `status`, `created_at`, `remark`)
VALUES
    (1, 2, '2026-06-20 14:00:00', '2026-06-22 12:00:00', 1, '已确认', NOW(), '需要安静房间'),
    (2, 2, '2026-06-21 14:00:00', '2026-06-23 12:00:00', 2, '已确认', NOW(), '需要双床'),
    (3, 1, '2026-06-25 14:00:00', '2026-06-27 12:00:00', 3, '已确认', NOW(), '行政接待');

INSERT INTO `reservation_details` (`reservation_id`, `type_id`, `room_count`)
VALUES
    (1, 1, 1),
    (2, 2, 1),
    (3, 3, 1);
