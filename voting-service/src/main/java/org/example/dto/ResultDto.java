package org.example.dto;


import java.util.ArrayList;
import java.util.List;

public class ResultDto {
    private Long electionId;
    private String electionTitle;
    private List<CandidateResultDto> results = new ArrayList<>();
    private int totalVotes;

    public Long getElectionId() {
        return electionId;
    }

    public void setElectionId(Long electionId) {
        this.electionId = electionId;
    }

    public String getElectionTitle() {
        return electionTitle;
    }

    public void setElectionTitle(String electionTitle) {
        this.electionTitle = electionTitle;
    }

    public List<CandidateResultDto> getResults() {
        return results;
    }

    public void setResults(List<CandidateResultDto> results) {
        this.results = results;
    }

    public int getTotalVotes() {
        return totalVotes;
    }

    public void setTotalVotes(int totalVotes) {
        this.totalVotes = totalVotes;
    }
}