# FlashMall 开发日志


## 2026-07-14 Day1 项目基础搭建


### 完成内容

1. 完成 Spring Boot 3 项目初始化

- Java版本：21
- Spring Boot版本：3.5.16
- Maven构建


2. 完成统一响应结果封装

新增：

- Result<T>
- ResultCode


实现：

- 成功响应
- 失败响应
- 泛型数据封装


3. 完成业务异常体系

新增：

- BusinessException


实现：

业务代码通过：

throw new BusinessException(ResultCode)

抛出异常。


4. 完成全局异常处理

新增：

- GlobalExceptionHandler


实现：

BusinessException自动转换为统一Result。


5. 完成Spring Security基础配置

开发阶段：

- 暂时关闭接口鉴权
- 所有请求允许访问


### 今日问题记录

1. Spring Security默认开启认证导致接口被拦截。

解决：

增加SecurityConfig进行开发阶段放行。


### 下一步计划

- MySQL数据库设计
- MyBatis-Plus集成
- 用户模块开发

Day1 实际耗时：2天
原因：首次搭建SpringBoot项目，对Java/Spring基础概念进行补充学习