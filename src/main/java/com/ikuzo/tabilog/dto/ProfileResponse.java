package com.ikuzo.tabilog.dto;

public record ProfileResponse(
        Long id,
        String userId,
        String nickname,
        String email
) {}
