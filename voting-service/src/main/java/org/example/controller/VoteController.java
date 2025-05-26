package org.example.controller;


import org.example.dto.ElectionResultDTO;
import org.example.dto.VoteDTO;
import org.example.service.VoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/voting/votes")
public class VoteController {
    private final VoteService voteService;

    public VoteController(VoteService voteService) {
        this.voteService = voteService;
    }

    @PostMapping
    @PreAuthorize("hasRole('VOTER')")
    public ResponseEntity<VoteDTO> castVote(
            @RequestBody VoteDTO voteDTO,
            @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(voteService.castVote(voteDTO, token));
    }

    @GetMapping("/results/{electionId}")
    public ResponseEntity<ElectionResultDTO> getElectionResults(
            @PathVariable Long electionId) {
        return ResponseEntity.ok(voteService.getElectionResults(electionId));
    }
}