package com.smartdialer.dialer.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.FileReader;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class DataIngestionService {

    private static final Logger log = LoggerFactory.getLogger(DataIngestionService.class);
    private final String DATASET_DIR = "C:/Users/uday1/OneDrive/Desktop/credresolve/collections_30k_dataset.zip";

    private final HistoricalMetricsService historicalMetricsService;

    public DataIngestionService(HistoricalMetricsService historicalMetricsService) {
        this.historicalMetricsService = historicalMetricsService;
    }

    @PostConstruct
    public void ingest() {
        log.info("Starting ingestion from real dataset...");
        ingestCalls();
    }

    private void ingestCalls() {
        Path path = Paths.get(DATASET_DIR, "calls.csv");
        try (Reader reader = new FileReader(path.toFile());
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {
             
             List<HistoricalMetricsService.HistoricalCall> calls = new ArrayList<>();
             for (CSVRecord record : csvParser) {
                 HistoricalMetricsService.HistoricalCall call = new HistoricalMetricsService.HistoricalCall();
                 call.setCallId(record.get("call_id"));
                 call.setCampaignId(Long.parseLong(record.get("campaign_id").replaceAll("[^0-9]", ""))); // ensure numeric
                 call.setProvider(record.get("vendor_id"));
                 call.setOutcome(record.get("call_status"));
                 call.setTalkTimeSeconds(Integer.parseInt(record.get("duration_sec")));
                 calls.add(call);
                 
                 if (calls.size() >= 5000) break; // Limit ingestion memory overhead for demo
             }
             historicalMetricsService.setHistoricalData(calls);
             log.info("Ingested {} calls for historical analytics.", calls.size());
        } catch (Exception e) {
             log.error("Failed to parse calls.csv", e);
        }
    }
}
