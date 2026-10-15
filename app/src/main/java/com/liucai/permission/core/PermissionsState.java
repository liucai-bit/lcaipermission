package com.liucai.permission.core;

/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/10/14
 */
public enum PermissionsState {
    /** 组内所有权限都从未申请过 */
    NEVER_REQUESTED,

    /** 组内部分申请过、部分没申请过 */
    PARTIALLY_REQUESTED,

    /** 组内全部已授权 */
    ALL_GRANTED,

    /** 组内全部拒绝，但至少一个还能再弹系统框 */
    ALL_DENIED_CAN_ASK,

    /** 组内全部永久拒绝（系统框弹不出来） */
    ALL_DENIED_PERMANENT,

    /** 组内混合：有的授权，有的拒绝，或有的永久拒绝 */
    MIXED
}
