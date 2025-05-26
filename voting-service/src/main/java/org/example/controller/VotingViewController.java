package org.example.controller;

import org.example.dto.CandidateDTO;
import org.example.dto.ElectionDTO;
import org.example.dto.ElectionResultDTO;
import org.example.dto.VoteDTO;
import org.example.service.CandidateService;
import org.example.service.ElectionService;
import org.example.service.VoteService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/voting")
public class VotingViewController {
    private final ElectionService electionService;
    private final CandidateService candidateService;
    private final VoteService voteService;

    public VotingViewController(ElectionService electionService,
                                CandidateService candidateService,
                                VoteService voteService) {
        this.electionService = electionService;
        this.candidateService = candidateService;
        this.voteService = voteService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false) String token, 
                           HttpServletRequest request, 
                           Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            String baseUrl = getBaseUrl(request);
            return "redirect:" + baseUrl + "/login?error=authfailed";
        }

        List<ElectionDTO> activeElections = electionService.getActiveElections();
        List<ElectionDTO> upcomingElections = electionService.getUpcomingElections();
        List<ElectionDTO> pastElections = electionService.getPastElections();

        model.addAttribute("activeElections", activeElections);
        model.addAttribute("upcomingElections", upcomingElections);
        model.addAttribute("pastElections", pastElections);
        model.addAttribute("token", token);
        model.addAttribute("fullname", authentication.getName().toUpperCase());
        return "dashboard";
    }

    @GetMapping("/vote/{id}")
    public String votePage(@PathVariable Long id,
                           @RequestParam(required = false) String token,
                           HttpServletRequest request,
                           Model model,
                           Authentication authentication) {
        if (!authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("VOTER"))) {
            String baseUrl = getBaseUrl(request);
            return "redirect:" + baseUrl + "/voting/dashboard?token=" + token + "&error=unauthorized";
        }

        ElectionDTO election = electionService.getElectionById(id);
        List<CandidateDTO> candidates = candidateService.getCandidatesByElection(id);

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(now, election.getEndTime());
        String timeRemaining = String.format("%d days, %d hours, %d minutes",
                duration.toDays(), duration.toHoursPart(), duration.toMinutesPart());

        VoteDTO vote = new VoteDTO();
        vote.setElectionId(id);
        vote.setVoterId(authentication.getName());

        model.addAttribute("election", election);
        model.addAttribute("candidates", candidates);
        model.addAttribute("vote", vote);
        model.addAttribute("timeRemaining", timeRemaining);
        model.addAttribute("token", token);
        return "vote";
    }

    @PostMapping("/vote")
    public String castVote(@ModelAttribute VoteDTO voteDTO,
                           @RequestParam(required = false) String token,
                           HttpServletRequest request,
                           Model model,
                           Authentication authentication) {
        try {
            System.out.println("===== CAST VOTE CONTROLLER =====");
            System.out.println("Vote DTO: " + voteDTO);
            System.out.println("Token: " + (token != null ? "YES" : "NO"));
            System.out.println("Authentication: " + (authentication != null ? authentication.getName() : "NULL"));
            System.out.println("Election ID: " + voteDTO.getElectionId());
            System.out.println("Candidate ID: " + voteDTO.getCandidateId());
            
            // Ensure token is properly formatted
            String authToken = token != null ?
                    (token.startsWith("Bearer ") ? token : "Bearer " + token) : "";

            // Set the voter ID from authentication
            voteDTO.setVoterId(authentication.getName());
            
            System.out.println("Calling voteService.castVote...");
            voteService.castVote(voteDTO, authToken);
            System.out.println("Vote cast successfully!");
            
            String baseUrl = getBaseUrl(request);
            String redirectUrl = baseUrl + "/voting/dashboard?token=" + token + "&voteSuccess";
            System.out.println("Redirecting to: " + redirectUrl);
            
            return "redirect:" + redirectUrl;
        } catch (Exception e) {
            System.err.println("Error in castVote controller: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            model.addAttribute("token", token);
            return votePage(voteDTO.getElectionId(), token, request, model, authentication);
        }
    }

    @GetMapping("/results/{id}")
    public String viewResults(@PathVariable Long id,
                              @RequestParam(required = false) String token,
                              Model model) {
        ElectionResultDTO results = voteService.getElectionResults(id);
        model.addAttribute("results", results);
        model.addAttribute("token", token);
        return "results";
    }

    private String getBaseUrl(HttpServletRequest request) {
        System.out.println("===== GET BASE URL =====");
        System.out.println("Request URL: " + request.getRequestURL());
        System.out.println("Server Name: " + request.getServerName());
        System.out.println("Server Port: " + request.getServerPort());
        System.out.println("Scheme: " + request.getScheme());
        
        // Check for forwarded headers (from Ingress)
        String originalHost = request.getHeader("X-Forwarded-Host");
        String originalProto = request.getHeader("X-Forwarded-Proto");
        
        System.out.println("X-Forwarded-Host: " + originalHost);
        System.out.println("X-Forwarded-Proto: " + originalProto);
        
        // Handle comma-separated values
        if (originalHost != null && originalHost.contains(",")) {
            originalHost = originalHost.split(",")[0].trim();
        }
        if (originalProto != null && originalProto.contains(",")) {
            originalProto = originalProto.split(",")[0].trim();
        }
        
        String baseUrl;
        
        // Use forwarded headers if available (coming through Ingress)
        if (originalHost != null && !originalHost.isEmpty()) {
            String scheme = originalProto != null && !originalProto.isEmpty() ? originalProto : "http";
            baseUrl = scheme + "://" + originalHost;
        } else {
            // Fallback to request details
            String scheme = request.getScheme();
            String serverName = request.getServerName();
            int serverPort = request.getServerPort();
            
            if ((scheme.equals("http") && serverPort == 80) || 
                (scheme.equals("https") && serverPort == 443)) {
                baseUrl = scheme + "://" + serverName;
            } else {
                baseUrl = scheme + "://" + serverName + ":" + serverPort;
            }
        }
        
        System.out.println("Final base URL: " + baseUrl);
        System.out.println("===== END GET BASE URL =====");
        
        return baseUrl;
    }
}