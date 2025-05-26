package org.example.service;


import org.example.dto.ElectionDTO;
import org.example.entities.Election;
import org.example.repository.ElectionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ElectionService {
    private final ElectionRepository electionRepository;

    public ElectionService(ElectionRepository electionRepository) {
        this.electionRepository = electionRepository;
    }

    public List<ElectionDTO> getAllElections() {
        return electionRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ElectionDTO> getActiveElections() {
        LocalDateTime now = LocalDateTime.now();
        return electionRepository.findByStartTimeBeforeAndEndTimeAfter(now, now).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ElectionDTO> getUpcomingElections() {
        return electionRepository.findByStartTimeAfter(LocalDateTime.now()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ElectionDTO> getPastElections() {
        return electionRepository.findByEndTimeBefore(LocalDateTime.now()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ElectionDTO createElection(ElectionDTO electionDTO) {
        Election election = new Election();
        election.setTitle(electionDTO.getTitle());
        election.setDescription(electionDTO.getDescription());
        election.setStartTime(electionDTO.getStartTime());
        election.setEndTime(electionDTO.getEndTime());
        election.setActive(true);

        Election savedElection = electionRepository.save(election);
        return convertToDTO(savedElection);
    }

    public ElectionDTO updateElection(Long id, ElectionDTO electionDTO) {
        Election election = electionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Election not found"));

        election.setTitle(electionDTO.getTitle());
        election.setDescription(electionDTO.getDescription());
        election.setStartTime(electionDTO.getStartTime());
        election.setEndTime(electionDTO.getEndTime());

        Election updatedElection = electionRepository.save(election);
        return convertToDTO(updatedElection);
    }

    public void deleteElection(Long id) {
        electionRepository.deleteById(id);
    }

    private ElectionDTO convertToDTO(Election election) {
        ElectionDTO dto = new ElectionDTO();
        dto.setId(election.getId());
        dto.setTitle(election.getTitle());
        dto.setDescription(election.getDescription());
        dto.setStartTime(election.getStartTime());
        dto.setEndTime(election.getEndTime());
        dto.setActive(election.isActive());
        return dto;
    }

    public ElectionDTO getElectionById(Long id) {
        Election election = electionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Election not found with id: " + id));
        return convertToDTO(election);
    }

}