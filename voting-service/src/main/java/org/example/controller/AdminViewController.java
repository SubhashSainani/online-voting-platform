package org.example.controller;

import org.example.dto.CandidateDTO;
import org.example.dto.ElectionDTO;
import org.example.service.CandidateService;
import org.example.service.ElectionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/voting")
public class AdminViewController {
    private final ElectionService electionService;
    private final CandidateService candidateService;

    public AdminViewController(ElectionService electionService, CandidateService candidateService) {
        this.electionService = electionService;
        this.candidateService = candidateService;
    }

    @GetMapping("/elections")
    public String manageElections(@RequestParam(required = false) String token, Model model) {
        List<ElectionDTO> elections = electionService.getAllElections();
        model.addAttribute("elections", elections);
        model.addAttribute("token", token);
        return "elections";
    }

    @PostMapping("/elections/create")
    public String createElection(@ModelAttribute ElectionDTO electionDTO, 
                                @RequestParam(required = false) String token,
                                HttpServletRequest request) {
        try {
            System.out.println("===== CREATE ELECTION =====");
            System.out.println("Election DTO: " + electionDTO);
            System.out.println("Token: " + (token != null ? "YES" : "NO"));
            
            electionService.createElection(electionDTO);
            System.out.println("Election created successfully!");
            
            String baseUrl = getBaseUrl(request);
            String redirectUrl = baseUrl + "/voting/elections?token=" + token + "&success";
            System.out.println("Redirecting to: " + redirectUrl);
            
            return "redirect:" + redirectUrl;
        } catch (Exception e) {
            System.err.println("Error creating election: " + e.getMessage());
            e.printStackTrace();
            String baseUrl = getBaseUrl(request);
            return "redirect:" + baseUrl + "/voting/elections?token=" + token + "&error=" + e.getMessage();
        }
    }

    @PostMapping("/elections/update")
    public String updateElection(@ModelAttribute ElectionDTO electionDTO, 
                                @RequestParam(required = false) String token,
                                HttpServletRequest request) {
        try {
            System.out.println("===== UPDATE ELECTION =====");
            System.out.println("Election DTO: " + electionDTO);
            System.out.println("Token: " + (token != null ? "YES" : "NO"));
            
            electionService.updateElection(electionDTO.getId(), electionDTO);
            System.out.println("Election updated successfully!");
            
            String baseUrl = getBaseUrl(request);
            String redirectUrl = baseUrl + "/voting/elections?token=" + token + "&success";
            System.out.println("Redirecting to: " + redirectUrl);
            
            return "redirect:" + redirectUrl;
        } catch (Exception e) {
            System.err.println("Error updating election: " + e.getMessage());
            e.printStackTrace();
            String baseUrl = getBaseUrl(request);
            return "redirect:" + baseUrl + "/voting/elections?token=" + token + "&error=" + e.getMessage();
        }
    }

    @GetMapping("/elections/{id}/delete")
    public String deleteElection(@PathVariable Long id, 
                                @RequestParam(required = false) String token,
                                HttpServletRequest request) {
        try {
            System.out.println("===== DELETE ELECTION =====");
            System.out.println("Election ID: " + id);
            System.out.println("Token: " + (token != null ? "YES" : "NO"));
            
            electionService.deleteElection(id);
            System.out.println("Election deleted successfully!");
            
            String baseUrl = getBaseUrl(request);
            String redirectUrl = baseUrl + "/voting/elections?token=" + token + "&success";
            System.out.println("Redirecting to: " + redirectUrl);
            
            return "redirect:" + redirectUrl;
        } catch (Exception e) {
            System.err.println("Error deleting election: " + e.getMessage());
            e.printStackTrace();
            String baseUrl = getBaseUrl(request);
            return "redirect:" + baseUrl + "/voting/elections?token=" + token + "&error=" + e.getMessage();
        }
    }

    @GetMapping("/candidates")
    public String manageCandidates(
            @RequestParam(required = false) Long electionId,
            @RequestParam(required = false) String token,
            Model model) {

        List<ElectionDTO> allElections = electionService.getAllElections();
        model.addAttribute("allElections", allElections);
        model.addAttribute("token", token);

        // Create a map for election names lookup
        Map<Long, String> electionMap = allElections.stream()
                .collect(Collectors.toMap(ElectionDTO::getId, ElectionDTO::getTitle));
        model.addAttribute("electionMap", electionMap);

        if (electionId != null) {
            ElectionDTO selectedElection = electionService.getElectionById(electionId);
            List<CandidateDTO> candidates = candidateService.getCandidatesByElection(electionId);
            model.addAttribute("candidates", candidates);
            model.addAttribute("selectedElection", selectedElection);
        } else {
            // Get all candidates across all elections
            List<CandidateDTO> allCandidates = allElections.stream()
                    .flatMap(election -> candidateService.getCandidatesByElection(election.getId()).stream())
                    .collect(Collectors.toList());
            model.addAttribute("candidates", allCandidates);
        }

        return "candidates";
    }

    @PostMapping("/candidates/create")
    public String createCandidate(@ModelAttribute CandidateDTO candidateDTO, 
                                 @RequestParam(required = false) String token,
                                 HttpServletRequest request) {
        try {
            System.out.println("===== CREATE CANDIDATE =====");
            System.out.println("Candidate DTO: " + candidateDTO);
            System.out.println("Token: " + (token != null ? "YES" : "NO"));
            
            candidateService.createCandidate(candidateDTO);
            System.out.println("Candidate created successfully!");
            
            String baseUrl = getBaseUrl(request);
            String redirectUrl = baseUrl + "/voting/candidates?token=" + token + "&success";
            System.out.println("Redirecting to: " + redirectUrl);
            
            return "redirect:" + redirectUrl;
        } catch (Exception e) {
            System.err.println("Error creating candidate: " + e.getMessage());
            e.printStackTrace();
            String baseUrl = getBaseUrl(request);
            return "redirect:" + baseUrl + "/voting/candidates?token=" + token + "&error=" + e.getMessage();
        }
    }

    @PostMapping("/candidates/update")
    public String updateCandidate(@ModelAttribute CandidateDTO candidateDTO, 
                                 @RequestParam(required = false) String token,
                                 HttpServletRequest request) {
        try {
            System.out.println("===== UPDATE CANDIDATE =====");
            System.out.println("Candidate DTO: " + candidateDTO);
            System.out.println("Token: " + (token != null ? "YES" : "NO"));
            
            candidateService.updateCandidate(candidateDTO.getId(), candidateDTO);
            System.out.println("Candidate updated successfully!");
            
            String baseUrl = getBaseUrl(request);
            String redirectUrl = baseUrl + "/voting/candidates?token=" + token + "&success";
            System.out.println("Redirecting to: " + redirectUrl);
            
            return "redirect:" + redirectUrl;
        } catch (Exception e) {
            System.err.println("Error updating candidate: " + e.getMessage());
            e.printStackTrace();
            String baseUrl = getBaseUrl(request);
            return "redirect:" + baseUrl + "/voting/candidates?token=" + token + "&error=" + e.getMessage();
        }
    }

    @GetMapping("/candidates/{id}/delete")
    public String deleteCandidate(@PathVariable Long id, 
                                 @RequestParam(required = false) String token,
                                 HttpServletRequest request) {
        try {
            System.out.println("===== DELETE CANDIDATE =====");
            System.out.println("Candidate ID: " + id);
            System.out.println("Token: " + (token != null ? "YES" : "NO"));
            
            candidateService.deleteCandidate(id);
            System.out.println("Candidate deleted successfully!");
            
            String baseUrl = getBaseUrl(request);
            String redirectUrl = baseUrl + "/voting/candidates?token=" + token + "&success";
            System.out.println("Redirecting to: " + redirectUrl);
            
            return "redirect:" + redirectUrl;
        } catch (Exception e) {
            System.err.println("Error deleting candidate: " + e.getMessage());
            e.printStackTrace();
            String baseUrl = getBaseUrl(request);
            return "redirect:" + baseUrl + "/voting/candidates?token=" + token + "&error=" + e.getMessage();
        }
    }

    private String getBaseUrl(HttpServletRequest request) {
        System.out.println("===== ADMIN GET BASE URL =====");
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
        System.out.println("===== END ADMIN GET BASE URL =====");
        
        return baseUrl;
    }
}