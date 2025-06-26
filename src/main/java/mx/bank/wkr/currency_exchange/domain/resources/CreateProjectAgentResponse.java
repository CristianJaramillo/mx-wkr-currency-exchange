package mx.bank.wkr.currency_exchange.domain.resources;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.List;

public record CreateProjectAgentResponse(
        @JsonProperty("id")
        String id,
        @JsonProperty("type")
        String type,
        @JsonProperty("status")
        String status,
        @JsonProperty("project")
        Project project,
        @JsonProperty("attachments")
        List<Attachment> attachments,
        @JsonProperty("createdAt")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "UTC")
        Date createdAt,
        @JsonProperty("updatedAt")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "UTC")
        Date updatedAt
) { }
