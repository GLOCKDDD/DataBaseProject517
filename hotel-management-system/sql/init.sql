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
    UNIQUE KEY `uk_username` (`username`)
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
    KEY `idx_name` (`name`)
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
    UNIQUE KEY `uk_type_name` (`type_name`)
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
    CONSTRAINT `fk_reservation_customer` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`customer_id`),
    CONSTRAINT `fk_reservation_user` FOREIGN KEY (`created_by`) REFERENCES `users` (`user_id`)
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
    KEY `idx_reservation_id` (`reservation_id`),
    KEY `idx_type_id` (`type_id`),
    CONSTRAINT `fk_detail_reservation` FOREIGN KEY (`reservation_id`) REFERENCES `reservations` (`reservation_id`),
    CONSTRAINT `fk_detail_room_type` FOREIGN KEY (`type_id`) REFERENCES `room_types` (`type_id`)
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
    CONSTRAINT `fk_checkin_reservation` FOREIGN KEY (`reservation_id`) REFERENCES `reservations` (`reservation_id`),
    CONSTRAINT `fk_checkin_room` FOREIGN KEY (`room_id`) REFERENCES `rooms` (`room_id`)
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
    KEY `idx_checkin_id` (`checkin_id`),
    KEY `idx_customer_id` (`customer_id`),
    CONSTRAINT `fk_guest_checkin` FOREIGN KEY (`checkin_id`) REFERENCES `checkins` (`checkin_id`),
    CONSTRAINT `fk_guest_customer` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`customer_id`)
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
    CONSTRAINT `fk_bill_checkin` FOREIGN KEY (`checkin_id`) REFERENCES `checkins` (`checkin_id`),
    CONSTRAINT `fk_bill_user` FOREIGN KEY (`created_by`) REFERENCES `users` (`user_id`)
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
    CONSTRAINT `fk_change_checkin` FOREIGN KEY (`checkin_id`) REFERENCES `checkins` (`checkin_id`),
    CONSTRAINT `fk_change_old_room` FOREIGN KEY (`old_room_id`) REFERENCES `rooms` (`room_id`),
    CONSTRAINT `fk_change_new_room` FOREIGN KEY (`new_room_id`) REFERENCES `rooms` (`room_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='换房记录表';

-- ============================================================
-- 初始化数据：默认管理员账号
-- 密码为 admin123 的 BCrypt 加盐哈希值
-- ============================================================
INSERT INTO `users` (`username`, `password_hash`, `role`, `permissions`, `created_at`)
VALUES ('admin', '$2a$10$EqKcp1WFKVQIShMPC7B3kuznX9gAZMsVnSNjN0ABNuHVBCpzqABae', 'admin', '*', NOW());
-- 默认管理员账号: admin / admin123
-- 首次登录后请立即修改密码
