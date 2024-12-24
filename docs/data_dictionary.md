# 景区门票管理系统数据字典

## 票种表 (ticket_types)

| 字段名         | 类型         | 约束           | 描述                    |
|--------------|--------------|----------------|------------------------|
| id           | Integer      | Primary Key    | 票种ID                 |
| name         | String(50)   | Not Null      | 票种名称                |
| price        | Float        | Not Null      | 票价                    |
| description  | Text         | Nullable      | 票种描述                |
| status       | Boolean      | Default True  | 票种状态(启用/禁用)      |
| min_group_size| Integer      | Nullable      | 团体票最小购买数量       |

## 营业员表 (operators)

| 字段名      | 类型         | 约束           | 描述                    |
|------------|--------------|----------------|------------------------|
| id         | Integer      | Primary Key    | 营业员ID               |
| name       | String(50)   | Not Null      | 姓名                    |
| username   | String(50)   | Unique, Not Null| 用户名                |
| password   | String(100)  | Not Null      | 密码(加密存储)          |
| status     | Boolean      | Default True  | 账号状态(启用/禁用)      |
| create_time| DateTime     | Default Now   | 创建时间                |

## 售票记录表 (ticket_sales)

| 字段名        | 类型         | 约束           | 描述                    |
|--------------|--------------|----------------|------------------------|
| id           | Integer      | Primary Key    | 销售记录ID             |
| ticket_type_id| Integer     | Foreign Key    | 票种ID                 |
| operator_id  | Integer      | Foreign Key    | 营业员ID               |
| quantity     | Integer      | Not Null      | 数量                    |
| total_amount | Float        | Not Null      | 总金额                  |
| sale_time    | DateTime     | Default Now   | 销售时间                |
| status       | Boolean      | Default True  | 状态(有效/已退票)        |

## 退票记录表 (refunds)

| 字段名        | 类型         | 约束           | 描述                    |
|--------------|--------------|----------------|------------------------|
| id           | Integer      | Primary Key    | 退票记录ID             |
| sale_id      | Integer      | Foreign Key    | 销售记录ID             |
| operator_id  | Integer      | Foreign Key    | 处理退票的营业员ID      |
| refund_time  | DateTime     | Default Now   | 退票时间                |
| reason       | Text         | Nullable      | 退票原因                |

## 销售日志表 (sales_logs)

| 字段名         | 类型         | 约束           | 描述                    |
|---------------|--------------|----------------|------------------------|
| id            | Integer      | Primary Key    | 日志ID                 |
| operation_type| Enum         | Not Null      | 操作类型(sale/refund)   |
| ticket_sale_id| Integer      | Foreign Key    | 销售记录ID             |
| operator_id   | Integer      | Foreign Key    | 营业员ID               |
| operation_time| DateTime     | Default Now   | 操作时间                |
| details       | Text         | Not Null      | 操作详情                |

## 每日销售统计表 (daily_sales_stats)

| 字段名            | 类型         | 约束           | 描述                    |
|------------------|--------------|----------------|------------------------|
| date            | String       | Primary Key    | 统计日期                |
| total_transactions| Integer     | Default 0     | 总交易数                |
| total_quantity  | Integer      | Default 0     | 总售票数                |
| total_amount    | Float        | Default 0.0   | 总金额                  |
| updated_at      | DateTime     | Default Now   | 更新时间                |

## 月度销售统计表 (monthly_sales_stats)

| 字段名            | 类型         | 约束           | 描述                    |
|------------------|--------------|----------------|------------------------|
| year_month      | String       | Primary Key    | 统计年月(YYYY-MM)       |
| total_transactions| Integer     | Default 0     | 总交易数                |
| total_quantity  | Integer      | Default 0     | 总售票数                |
| total_amount    | Float        | Default 0.0   | 总金额                  |
| updated_at      | DateTime     | Default Now   | 更新时间                |

## 票种销售统计表 (ticket_type_sales_stats)

| 字段名         | 类型         | 约束           | 描述                    |
|---------------|--------------|----------------|------------------------|
| id            | Integer      | Primary Key    | 统计记录ID             |
| date          | String       | Not Null      | 统计日期                |
| ticket_type_id| Integer      | Foreign Key    | 票种ID                 |
| transactions  | Integer      | Default 0     | 交易数                  |
| quantity      | Integer      | Default 0     | 售票数                  |
| amount        | Float        | Default 0.0   | 金额                    |
| updated_at    | DateTime     | Default Now   | 更新时间                |


## 营业员销售统计表 (operator_sales_stats)

| 字段名         | 类型         | 约束           | 描述                    |
|---------------|--------------|----------------|------------------------|
| id            | Integer      | Primary Key    | 统计记录ID             |
| date          | String       | Not Null      | 统计日期                |
| operator_id   | Integer      | Foreign Key    | 营业员ID               |
| transactions  | Integer      | Default 0     | 交易数                  |
| quantity      | Integer      | Default 0     | 售票数                  |
| amount        | Float        | Default 0.0   | 金额                    |
| updated_at    | DateTime     | Default Now   | 更新时间                |
