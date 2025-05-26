package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ElectionResultDTO {
    private Long electionId;
    private String electionTitle;
    private Map<String, Integer> candidateVotes;
    private int totalVotes;
}