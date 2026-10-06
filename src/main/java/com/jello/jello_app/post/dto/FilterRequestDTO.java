package com.jello.jello_app.post.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FilterRequestDTO {
    private boolean filterAi;
    private List<Long> tags;
    private LocalDate initialDate;
    private LocalDate finalDate;
    private String search;
}
