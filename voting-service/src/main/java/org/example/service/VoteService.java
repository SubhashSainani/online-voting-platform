package org.example.service;


import org.example.client.UserServiceClient;
import org.example.dto.ElectionResultDTO;
import org.example.dto.UserDTO;
import org.example.dto.VoteDTO;
import org.example.entities.Candidate;
import org.example.entities.Election;
import org.example.entities.Vote;
import org.example.repository.CandidateRepository;
import org.example.repository.ElectionRepository;
import org.example.repository.VoteRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class VoteService {
    private final VoteRepository voteRepository;
    private final ElectionRepository electionRepository;
    private final CandidateRepository candidateRepository;
    private final UserServiceClient userServiceClient;

    public VoteService(VoteRepository voteRepository,
                       ElectionRepository electionRepository,
                       CandidateRepository candidateRepository,
                       UserServiceClient userServiceClient) {
        this.voteRepository = voteRepository;
        this.electionRepository = electionRepository;
        this.candidateRepository = candidateRepository;
        this.userServiceClient = userServiceClient;
    }

    public VoteDTO castVote(VoteDTO voteDTO, String token) {
        // Verify election exists and is active
        Election election = electionRepository.findById(voteDTO.getElectionId())
                .orElseThrow(() -> new RuntimeException("Election not found"));

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(election.getStartTime())) {
            throw new RuntimeException("Election has not started yet");
        }
        if (now.isAfter(election.getEndTime())) {
            throw new RuntimeException("Election has already ended");
        }

        // Verify candidate exists in this election
        Candidate candidate = candidateRepository.findById(voteDTO.getCandidateId())
                .orElseThrow(() -> new RuntimeException("Candidate not found"));

        if (!candidate.getElection().getId().equals(election.getId())) {
            throw new RuntimeException("Candidate does not belong to this election");
        }

        // Verify user exists and is a voter
        try {
            // Ensure token starts with "Bearer "
            String authToken = token.startsWith("Bearer ") ? token : "Bearer " + token;

            ResponseEntity<UserDTO> response = userServiceClient.getUserByUsername(authToken, voteDTO.getVoterId());
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new RuntimeException("Failed to verify voter");
            }

            UserDTO user = response.getBody();
            if (!"VOTER".equals(user.getRole())) {
                throw new RuntimeException("Only voters can cast votes");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to verify voter: " + e.getMessage());
        }

        // Check if user has already voted in this election
        Optional<Vote> existingVote = voteRepository.findByVoterIdAndElectionId(
                voteDTO.getVoterId(), voteDTO.getElectionId());
        if (existingVote.isPresent()) {
            throw new RuntimeException("You have already voted in this election");
        }

        // Create and save the vote
        Vote vote = new Vote();
        vote.setVoterId(voteDTO.getVoterId());
        vote.setElection(election);
        vote.setCandidate(candidate);
        vote.setVotedAt(LocalDateTime.now());

        Vote savedVote = voteRepository.save(vote);
        return convertToDTO(savedVote);
    }

    public ElectionResultDTO getElectionResults(Long electionId) {
        Election election = electionRepository.findById(electionId)
                .orElseThrow(() -> new RuntimeException("Election not found"));

        List<Candidate> candidates = candidateRepository.findByElectionId(electionId);
        Map<String, Integer> candidateVotes = new HashMap<>();

        for (Candidate candidate : candidates) {
            int voteCount = voteRepository.countByCandidateId(candidate.getId());
            candidateVotes.put(candidate.getName(), voteCount);
        }

        int totalVotes = voteRepository.countByElectionId(electionId);

        ElectionResultDTO result = new ElectionResultDTO();
        result.setElectionId(electionId);
        result.setElectionTitle(election.getTitle());
        result.setCandidateVotes(candidateVotes);
        result.setTotalVotes(totalVotes);

        return result;
    }

    private VoteDTO convertToDTO(Vote vote) {
        VoteDTO dto = new VoteDTO();
        dto.setElectionId(vote.getElection().getId());
        dto.setCandidateId(vote.getCandidate().getId());
        dto.setVoterId(vote.getVoterId());
        dto.setVotedAt(vote.getVotedAt());
        return dto;
    }
}
