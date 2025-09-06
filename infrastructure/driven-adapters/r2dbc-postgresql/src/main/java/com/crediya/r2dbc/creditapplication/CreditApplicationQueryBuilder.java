package com.crediya.r2dbc.creditapplication;

import com.crediya.model.creditapplication.PaginationCreditApplicationFilter;
import org.springframework.r2dbc.core.DatabaseClient;

import static com.crediya.r2dbc.constants.CreditApplicationPaginationConstants.*;

public class CreditApplicationQueryBuilder {
    private String projection = null;
    private boolean withOrder = false;
    private boolean withPagination = false;
    private String sortBy = COL_AMOUNT;
    private String sortDirection = "ASC";

    public CreditApplicationQueryBuilder selectPaginatedFields() {
        this.projection = "app.amount, app.term, app.email, app.client_name AS clientName, ct.name AS creditType, ct.interest_rate AS interestRate, st.name AS stateApplication, app.client_salary_base AS salaryBase, ROUND((app.amount + (app.amount * (ct.interest_rate / 100) * (app.term / 12.0))) / app.term, 2) AS monthlyAmount";
        this.withOrder = true;
        this.withPagination = true;
        return this;
    }

    public CreditApplicationQueryBuilder selectCount() {
        this.projection = "COUNT(*)";
        this.withOrder = false;
        this.withPagination = false;
        return this;
    }

    public CreditApplicationQueryBuilder sortBy(String sortBy) {
        this.sortBy = sortBy;
        return this;
    }

    public CreditApplicationQueryBuilder sortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
        return this;
    }

    public String build() {
        String selectClause = projection != null ? projection : "*";
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ").append(selectClause).append("\n");
        sb.append("FROM application app\n");
        sb.append("JOIN credit_type ct ON ct.id_credit_type = app.id_credit_type\n");
        sb.append("JOIN state st ON st.id_state = app.id_state\n");
        sb.append("WHERE (:stateApplication IS NULL OR st.name = :stateApplication)\n");
        sb.append("AND (:isAutoEvaluation IS NULL OR ct.auto_validation = :isAutoEvaluation)\n");
        if (withOrder) {
            String safeSortBy = switch (sortBy) {
                case COL_AMOUNT,
                     COL_TERM,
                     COL_EMAIL,
                     COL_CLIENT_NAME,
                     COL_CREDIT_TYPE,
                     COL_INTEREST_RATE,
                     COL_STATE_APPLICATION,
                     COL_SALARY_BASE,
                     COL_MONTHLY_AMOUNT -> sortBy;
                default -> COL_AMOUNT;
            };
            String safeSortDirection = "ASC";
            if ("DESC".equalsIgnoreCase(sortDirection)) {
                safeSortDirection = "DESC";
            }
            sb.append(String.format("ORDER BY %s %s ", safeSortBy, safeSortDirection));
        }
        if (withPagination) {
            sb.append("LIMIT :limit OFFSET :offset ");
        }
        return sb.toString();
    }

    public static DatabaseClient.GenericExecuteSpec bindParams(DatabaseClient.GenericExecuteSpec spec, PaginationCreditApplicationFilter filter) {
        if (filter != null) {
            if (filter.getStatus() != null) {
                spec = spec.bind(PARAM_STATE_APPLICATION, filter.getStatus().name());
            } else {
                spec = spec.bindNull(PARAM_STATE_APPLICATION, String.class);
            }
            if (filter.getAutoEvaluation() != null) {
                spec = spec.bind(PARAM_IS_AUTO_EVALUATION, filter.getAutoEvaluation());
            } else {
                spec = spec.bindNull(PARAM_IS_AUTO_EVALUATION, Boolean.class);
            }
        } else {
            spec = spec.bindNull(PARAM_STATE_APPLICATION, String.class);
            spec = spec.bindNull(PARAM_IS_AUTO_EVALUATION, Boolean.class);
        }
        return spec;
    }
}
