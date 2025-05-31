package com.unihub.app.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ComentarioVoteRequest {
    @NotBlank // "UPVOTE" ou "DOWNVOTE"
    private String type;
} 