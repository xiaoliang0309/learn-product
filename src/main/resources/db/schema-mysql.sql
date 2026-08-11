-- ============================================
-- MySQL 建表脚本
-- ============================================

CREATE TABLE IF NOT EXISTS merchant (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name   VARCHAR(200) NOT NULL COMMENT '商户全称',
    short_name  VARCHAR(100) COMMENT '商户简称',
    biz_type    TINYINT NOT NULL DEFAULT 1 COMMENT '收款方式: 1-自收 2-代收',
    currency    VARCHAR(10) NOT NULL DEFAULT 'USD' COMMENT '交易货币',
    country     VARCHAR(50) NOT NULL DEFAULT 'US' COMMENT '国家',
    state       VARCHAR(50) COMMENT '州/省',
    city        VARCHAR(100) COMMENT '城市',
    address     VARCHAR(300) COMMENT '地址',
    email       VARCHAR(100) COMMENT '邮箱',
    phone       VARCHAR(30) COMMENT '电话',
    status      TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户表';

CREATE TABLE IF NOT EXISTS onboarding_record (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    merchant_id     BIGINT NOT NULL COMMENT '商户ID',
    receive_type    TINYINT NOT NULL COMMENT '收款类型: 1-自收 2-代收',
    payment_type    TINYINT NOT NULL COMMENT '支付类型: 3-PAX 4-Wizar 5-Qingo',
    apply_no        VARCHAR(64) COMMENT '申请单号',
    channel_mct_no  VARCHAR(100) COMMENT '渠道商户号',
    form_data       TEXT COMMENT '进件表单数据(JSON)',
    apply_status    TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-草稿 1-审核中 2-已入驻 3-驳回',
    remark          VARCHAR(500) COMMENT '备注',
    created_by      VARCHAR(100) COMMENT '创建人',
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='进件记录表';

CREATE TABLE IF NOT EXISTS trade_order (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no        VARCHAR(64) NOT NULL COMMENT '订单号',
    merchant_id     BIGINT NOT NULL COMMENT '商户ID',
    shop_id         BIGINT COMMENT '店铺ID',
    amount          BIGINT NOT NULL COMMENT '交易金额(分)',
    currency        VARCHAR(10) NOT NULL DEFAULT 'USD' COMMENT '货币',
    receive_type    TINYINT NOT NULL COMMENT '收款类型: 1-自收 2-代收',
    status          TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-待支付 1-已支付 2-已退款',
    payment_method  VARCHAR(30) COMMENT '支付方式: WECHAT/ALIPAY/STRIPE',
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';