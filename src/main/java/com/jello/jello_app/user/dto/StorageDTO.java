package com.jello.jello_app.user.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StorageDTO {
    private Long usedBytes;
    private Long totalBytes;
    private double usedPercentage;
}
