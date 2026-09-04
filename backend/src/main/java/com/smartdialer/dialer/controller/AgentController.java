package com.smartdialer.dialer.controller;

import com.smartdialer.dialer.model.Agent;
import com.smartdialer.dialer.service.AgentService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/agents")
@CrossOrigin(origins = "*")
public class AgentController {

    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    @GetMapping
    public List<Agent> getAllAgents() {
        return agentService.getAllAgents();
    }
}
