package com.churchflow360.authservice.model;

/**
 * Church roles in ascending privilege order.
 *
 * <ul>
 *   <li>MEMBER  — basic access: own profile, attendance, events, giving history</li>
 *   <li>LEADER  — MEMBER + manage small groups, create events, view attendance</li>
 *   <li>PASTOR  — LEADER + pastoral meetings, finance reports, member records</li>
 *   <li>ADMIN   — full access: user management, all reports, system config</li>
 * </ul>
 */
public enum Role {
    MEMBER,
    LEADER,
    PASTOR,
    ADMIN
}