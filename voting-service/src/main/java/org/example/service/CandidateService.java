package org.example.service;


import org.example.dto.CandidateDTO;
import org.example.entities.Candidate;
import org.example.entities.Election;
import org.example.repository.CandidateRepository;
import org.example.repository.ElectionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CandidateService {
    private final CandidateRepository candidateRepository;
    private final ElectionRepository electionRepository;

    public CandidateService(CandidateRepository candidateRepository,
                            ElectionRepository electionRepository) {
        this.candidateRepository = candidateRepository;
        this.electionRepository = electionRepository;
    }

    public List<CandidateDTO> getCandidatesByElection(Long electionId) {
        return candidateRepository.findByElectionId(electionId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public CandidateDTO createCandidate(CandidateDTO candidateDTO) {
        Election election = electionRepository.findById(candidateDTO.getElectionId())
                .orElseThrow(() -> new RuntimeException("Election not found"));

        Candidate candidate = new Candidate();
        candidate.setName(candidateDTO.getName());
        candidate.setParty(candidateDTO.getParty());
        candidate.setBio(candidateDTO.getBio());
        candidate.setElection(election);

        Candidate savedCandidate = candidateRepository.save(candidate);
        return convertToDTO(savedCandidate);
    }

    public CandidateDTO updateCandidate(Long id, CandidateDTO candidateDTO) {
        Candidate candidate = candidateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidate not found"));

        candidate.setName(candidateDTO.getName());
        candidate.setParty(candidateDTO.getParty());
        candidate.setBio(candidateDTO.getBio());

        Candidate updatedCandidate = candidateRepository.save(candidate);
        return convertToDTO(updatedCandidate);
    }

    public void deleteCandidate(Long id) {
        candidateRepository.deleteById(id);
    }

    private CandidateDTO convertToDTO(Candidate candidate) {
        CandidateDTO dto = new CandidateDTO();
        dto.setId(candidate.getId());
        dto.setName(candidate.getName());
        dto.setParty(candidate.getParty());
        dto.setBio(candidate.getBio());
        dto.setElectionId(candidate.getElection().getId());
        return dto;
    }
}