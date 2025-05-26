package org.example.repository;

import org.example.entities.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
    Optional<Vote> findByVoterIdAndElectionId(String voterId, Long electionId);
    List<Vote> findByElectionId(Long electionId);
    int countByElectionId(Long electionId);
    int countByCandidateId(Long candidateId);
}