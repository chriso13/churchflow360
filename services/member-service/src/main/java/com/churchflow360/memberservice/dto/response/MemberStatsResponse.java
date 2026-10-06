package com.churchflow360.memberservice.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MemberStatsResponse {
    private long total;
    private long active;
    private long inactive;
    private long visitors;
    private long transferred;
    private long deceased;
}