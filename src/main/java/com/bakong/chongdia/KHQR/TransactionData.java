package com.bakong.chongdia.KHQR;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionData {
    private String hash;
    private String fromAccountId;
    private String toAccountId;
    private String currency;
    private Double amount;
    private String description;
    private Double createdDateMs;
    private Double acknowledgedDateMs;
}
