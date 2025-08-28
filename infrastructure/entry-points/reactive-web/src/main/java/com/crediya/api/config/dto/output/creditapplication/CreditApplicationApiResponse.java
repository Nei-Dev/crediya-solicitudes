package com.crediya.api.config.dto.output.creditapplication;

import com.crediya.api.config.dto.output.ApiResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CreditApplicationApiResponse extends ApiResponseDTO<CreditApplicationResponse> {
	
	public CreditApplicationApiResponse(CreditApplicationResponse data, String message) {
		super(data, message);
	}
	
	public static CreditApplicationApiResponse of(CreditApplicationResponse data, String message) {
		return new CreditApplicationApiResponse(data, message);
	}
}
