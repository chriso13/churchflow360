package com.churchflow360.pastoralservice.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MeetingStatsResponse {
    private long totalMeetings;
    private long requested;
    private long confirmed;
    private long completed;
    private long cancelled;
    private long noShow;
    private long rescheduled;
    private long pendingFollowUp;
}