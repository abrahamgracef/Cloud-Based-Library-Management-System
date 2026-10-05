package com.library.management.service;

import com.library.management.dto.DevOpsStatusDto;
import com.library.management.dto.StatsSummaryDto;

public interface StatsService {
    StatsSummaryDto getStatsSummary();
    DevOpsStatusDto getDevOpsStatus();
}
