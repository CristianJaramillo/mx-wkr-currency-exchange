package mx.bank.wkr.currency_exchange.domain.resources;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record CreateProjectAgentRequest(
        @JsonProperty("id")
        String type,
        @JsonProperty("project")
        Project project,
        @JsonProperty("attachments")
        List<Attachment> attachments
) {}
