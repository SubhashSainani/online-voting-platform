package org.example.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoteDTO {
    private Long electionId;
    private Long candidateId;
    private String voterId;
    private LocalDateTime votedAt;
}