package it.skillswap.web.api.dto;

import java.util.List;

public record CommunityDto(
        List<OfferDto> activeOffers,
        List<RequestDto> openRequests,
        List<OfferDto> myOffers,
        List<RequestDto> myRequests) {}
