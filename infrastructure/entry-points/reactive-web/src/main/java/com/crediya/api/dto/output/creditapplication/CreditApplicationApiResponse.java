package com.crediya.api.dto.output.creditapplication;

import com.crediya.api.dto.output.ApiResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CreditApplicationApiResponse extends ApiResponse<CreditApplicationResponse> {
	
	public CreditApplicationApiResponse(CreditApplicationResponse data, String message) {
		super(data, message);
	}
	
	public static CreditApplicationApiResponse of(CreditApplicationResponse data, String message) {
		return new CreditApplicationApiResponse(data, message);
	}
}
