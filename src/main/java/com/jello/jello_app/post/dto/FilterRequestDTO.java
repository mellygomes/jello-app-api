package com.jello.jello_app.post.dto;

import com.jello.jello_app.enumeration.PostSort;
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
    private String search;
    private List<Long> tags;
    private LocalDate initialDate;
    private LocalDate finalDate;
    // RECENT (padrao), OLDEST, POPULAR ou COMMENTED
    private PostSort sort = PostSort.RECENT;
}
