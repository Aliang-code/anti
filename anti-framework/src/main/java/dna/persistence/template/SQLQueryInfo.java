package dna.persistence.template;

import dna.origins.annotation.QueryType;

import java.io.Serializable;
import java.util.Map;

public class SQLQueryInfo implements Serializable {
    private static final long serialVersionUID = 3606738168691683501L;
    private String queryString;
    private Map<String, Object> parameters;
    private QueryType queryType;

    public SQLQueryInfo() {
    }

    public SQLQueryInfo(String queryString, Map<String, Object> parameters, QueryType queryType) {
        this.queryString = queryString;
        this.parameters = parameters;
        this.queryType = queryType;
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public QueryType getQueryType() {
        return queryType;
    }

    public void setQueryType(QueryType queryType) {
        this.queryType = queryType;
    }
}
