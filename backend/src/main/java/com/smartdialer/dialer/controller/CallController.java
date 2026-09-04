package com.smartdialer.dialer.controller;

import com.smartdialer.dialer.model.Call;
import com.smartdialer.dialer.service.CallService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/calls")
@CrossOrigin(origins = "*")
public class CallController {

    private final CallService callService;

    public CallController(CallService callService) {
        this.callService = callService;
    }

    @GetMapping
    public List<Call> getAllCalls() {
        return callService.getAllCalls();
    }
}
