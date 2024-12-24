# 景区门票管理系统 E-R 图及关系模型

## 实体及其属性

### 主要实体

1. 票种 (TicketType)
   - ID [主键]
   - 名称 (name) [必填]
   - 价格 (price) [必填]
   - 描述 (description)
   - 状态 (status) [默认启用]
   - 最小团体人数 (min_group_size)

2. 营业员 (Operator)
   - ID [主键]
   - 姓名 (name) [必填]
   - 用户名 (username) [必填, 唯一]
   - 密码 (password) [必填]
   - 状态 (status) [默认启用]
   - 创建时间 (create_time) [自动]

3. 售票记录 (TicketSale)
   - ID [主键]
   - 票种ID [外键 -> TicketType]
   - 营业员ID [外键 -> Operator]
   - 数量 (quantity) [必填]
   - 总金额 (total_amount) [必填]
   - 销售时间 (sale_time) [自动]
   - 状态 (status) [默认有效]

4. 退票记录 (Refund)
   - ID [主键]
   - 售票记录ID [外键 -> TicketSale, 唯一]
   - 营业员ID [外键 -> Operator]
   - 退票时间 (refund_time) [自动]
   - 原因 (reason)

5. 销售日志 (SalesLog)
   - ID [主键]
   - 操作类型 (operation_type) [必填]
   - 售票记录ID [外键 -> TicketSale]
   - 营业员ID [外键 -> Operator]
   - 操作时间 (operation_time) [自动]
   - 详情 (details) [必填]

### 统计实体

6. 每日销售统计 (DailySalesStats)
   - 日期 [主键]
   - 总交易数 (total_transactions)
   - 总数量 (total_quantity)
   - 总金额 (total_amount)
   - 更新时间 (updated_at)

7. 月度销售统计 (MonthlySalesStats)
   - 年月 [主键]
   - 总交易数 (total_transactions)
   - 总数量 (total_quantity)
   - 总金额 (total_amount)
   - 更新时间 (updated_at)

8. 票种销售统计 (TicketTypeSalesStats)
   - ID [主键]
   - 日期 (date)
   - 票种ID [外键 -> TicketType]
   - 交易数 (transactions)
   - 数量 (quantity)
   - 金额 (amount)
   - 更新时间 (updated_at)

9. 营业员销售统计 (OperatorSalesStats)
   - ID [主键]
   - 日期 (date)
   - 营业员ID [外键 -> Operator]
   - 交易数 (transactions)
   - 数量 (quantity)
   - 金额 (amount)
   - 更新时间 (updated_at)

## 实体关系

### 核心业务关系

1. 售票关系
   - TicketType (1) --[提供]--> (N) TicketSale
   描述：一种票可以有多个销售记录，每个销售记录对应一种票

   - Operator (1) --[销售]--> (N) TicketSale
   描述：一个营业员可以有多个销售记录，每个销售记录由一个营业员创建

2. 退票关系
   - TicketSale (1) --[退票]--> (0/1) Refund
   描述：一个销售记录最多有一个退票记录

   - Operator (1) --[处理]--> (N) Refund
   描述：一个营业员可以处理多个退票

3. 日志关系
   - TicketSale (1) --[记录]--> (N) SalesLog
   描述：一个销售记录可以有多个相关日志（创建、退票等）

   - Operator (1) --[操作]--> (N) SalesLog
   描述：一个营业员的所有操作都会记录在日志中

### 统计关系

4. 票种统计关系
   - TicketType (1) --[统计]--> (N) TicketTypeSalesStats
   描述：一种票可以有多个统计记录（不同日期）

5. 营业员统计关系
   - Operator (1) --[统计]--> (N) OperatorSalesStats
   描述：一个营业员可以有多个统计记录（不同日期）

## E-R 图

```
[TicketType] 1──────N [TicketSale] 1──────0/1 [Refund]
     │                    │    │              │
     │                    │    │              │
     │                    │    │              │
     N                    N    N              N
[TicketTypeSalesStats]  [SalesLog]    [OperatorSalesStats]
                            │
                            │
                            1
                       [Operator]
```

## 关系模式转换

### 主实体表
1. ticket_types (
   id [PK],
   name [NN],
   price [NN],
   description,
   status [D=true],
   min_group_size
)

2. operators (
   id [PK],
   name [NN],
   username [NN, UQ],
   password [NN],
   status [D=true],
   create_time [D=now()]
)

3. ticket_sales (
   id [PK],
   ticket_type_id [FK->ticket_types.id, NN],
   operator_id [FK->operators.id, NN],
   quantity [NN],
   total_amount [NN],
   sale_time [D=now()],
   status [D=true]
)

4. refunds (
   id [PK],
   sale_id [FK->ticket_sales.id, UQ],
   operator_id [FK->operators.id, NN],
   refund_time [D=now()],
   reason
)

5. sales_logs (
   id [PK],
   operation_type [NN],
   ticket_sale_id [FK->ticket_sales.id],
   operator_id [FK->operators.id],
   operation_time [D=now()],
   details [NN]
)

### 统计表
6. daily_sales_stats (
   date [PK],
   total_transactions [D=0],
   total_quantity [D=0],
   total_amount [D=0.0],
   updated_at [D=now()]
)

7. monthly_sales_stats (
   year_month [PK],
   total_transactions [D=0],
   total_quantity [D=0],
   total_amount [D=0.0],
   updated_at [D=now()]
)

8. ticket_type_sales_stats (
   id [PK],
   date [NN],
   ticket_type_id [FK->ticket_types.id],
   transactions [D=0],
   quantity [D=0],
   amount [D=0.0],
   updated_at [D=now()]
)

9. operator_sales_stats (
   id [PK],
   date [NN],
   operator_id [FK->operators.id],
   transactions [D=0],
   quantity [D=0],
   amount [D=0.0],
   updated_at [D=now()]
)

注：
- [PK] = Primary Key (主键)
- [FK] = Foreign Key (外键)
- [NN] = Not Null (非空)
- [UQ] = Unique (唯一)
- [D=x] = Default Value (默认值)

## 关系模式 (Relational Schema)

1. TicketType (
   id [PK],
   name,
   price,
   description,
   status
)

2. Operator (
   id [PK],
   name,
   username,
   password,
   status,
   create_time
)

3. TicketSale (
   id [PK],
   ticket_type_id [FK -> TicketType.id],
   operator_id [FK -> Operator.id],
   quantity,
   total_amount,
   sale_time,
   status
)

4. Refund (
   id [PK],
   sale_id [FK -> TicketSale.id],
   operator_id [FK -> Operator.id],
   refund_time,
   reason
)

5. SalesLog (
   id [PK],
   operation_type,
   ticket_sale_id [FK -> TicketSale.id],
   operator_id [FK -> Operator.id],
   operation_time,
   details
)
