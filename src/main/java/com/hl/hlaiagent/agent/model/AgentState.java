package com.hl.hlaiagent.agent.model;

/**
 * 代理状态枚举类，定义了代理的不同状态，如空闲、执行中、等待输入等。每个状态可以对应不同的行为和处理逻辑，帮助管理代理的生命周期和状态转换。
 */
public enum AgentState {

    IDLE,           // 空闲状态，代理没有正在执行的任务
    RUNNING,      // 执行中状态，代理正在执行任务
    WAITING_INPUT,  // 等待输入状态，代理需要用户输入或外部数据才能继续执行
    FINISHED,      // 已完成状态，代理已经完成了当前任务
    ERROR           // 错误状态，代理在执行过程中发生了错误
}
