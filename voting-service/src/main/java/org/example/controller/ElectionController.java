package org.example.controller;


import org.example.dto.ElectionDTO;
import org.example.service.ElectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/voting/elections")
public class ElectionController {
    private final ElectionService electionService;

    public ElectionController(ElectionService electionService) {
        this.electionService = electionService;
    }

    @GetMapping
    public ResponseEntity<List<ElectionDTO>> getAllElections() {
        return ResponseEntity.ok(electionService.getAllElections());
    }

    @GetMapping("/active")
    public ResponseEntity<List<ElectionDTO>> getActiveElections() {
        return ResponseEntity.ok(electionService.getActiveElections());
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<ElectionDTO>> getUpcomingElections() {
        return ResponseEntity.ok(electionService.getUpcomingElections());
    }

    @GetMapping("/past")
    public ResponseEntity<List<ElectionDTO>> getPastElections() {
        return ResponseEntity.ok(electionService.getPastElections());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ElectionDTO> createElection(@RequestBody ElectionDTO electionDTO) {
        return ResponseEntity.ok(electionService.createElection(electionDTO));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ElectionDTO> updateElection(
            @PathVariable Long id, @RequestBody ElectionDTO electionDTO) {
        return ResponseEntity.ok(electionService.updateElection(id, electionDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteElection(@PathVariable Long id) {
        electionService.deleteElection(id);
        return ResponseEntity.noContent().build();
    }
}