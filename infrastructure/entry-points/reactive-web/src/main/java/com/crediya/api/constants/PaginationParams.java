package com.crediya.api.constants;

import com.crediya.model.helpers.SortDirection;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PaginationParams {
	
	public static final String PARAM_PAGE = "page";
	public static final String PARAM_SIZE = "size";
	public static final String PARAM_SORT_BY = "sortBy";
	public static final String PARAM_DIRECTION = "direction";
	public static final String PARAM_STATUS = "status";
	public static final String PARAM_AUTO_EVALUATION = "autoEvaluation";
	
	public static final int DEFAULT_PAGE = 1;
	public static final int DEFAULT_SIZE = 10;
	public static final String DEFAULT_SORT_BY = "amount";
	public static final String DEFAULT_DIRECTION = SortDirection.ASC.name();
	
}
