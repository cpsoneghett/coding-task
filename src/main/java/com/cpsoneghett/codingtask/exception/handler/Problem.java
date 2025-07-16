package com.cpsoneghett.codingtask.exception.handler;

import com.cpsoneghett.codingtask.exception.CustomError;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Problem {

    private final Integer status;
    private final LocalDateTime timestamp;
    private final String type;
    private final String title;
    private final List<CustomError> errors;

    private Problem(ProblemBuilder builder) {
        this.status = builder.status;
        this.timestamp = builder.timestamp;
        this.type = builder.type;
        this.title = builder.title;
        this.errors = builder.errors;
    }

    public static ProblemBuilder builder() {
        return new ProblemBuilder();
    }

    public Integer getStatus() {
        return status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public List<CustomError> getErrors() {
        return errors;
    }

    @Override
    public String toString() {
        return "Problem{" +
                "status=" + status +
                ", timestamp=" + timestamp +
                ", type='" + type + '\'' +
                ", title='" + title + '\'' +
                ", errors=" + errors +
                '}';
    }

    public static class ProblemBuilder {
        private Integer status;
        private LocalDateTime timestamp;
        private String type;
        private String title;
        private List<CustomError> errors;

        private ProblemBuilder() {
            this.timestamp = LocalDateTime.now();
        }

        public ProblemBuilder status(Integer status) {
            this.status = status;
            return this;
        }

        public ProblemBuilder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public ProblemBuilder type(String type) {
            this.type = type;
            return this;
        }

        public ProblemBuilder title(String title) {
            this.title = title;
            return this;
        }

        public ProblemBuilder errors(List<CustomError> errors) {
            this.errors = errors;
            return this;
        }

        public Problem build() {
            return new Problem(this);
        }
    }
}
