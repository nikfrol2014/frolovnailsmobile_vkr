package com.example.frolovnails.common;

public abstract class Resource<T> {
    private Resource() {}

    public static final class Loading<T> extends Resource<T> {
        private Loading() {}
        public static <T> Loading<T> getInstance() { return new Loading<>(); }
    }

    public static final class Success<T> extends Resource<T> {
        private final T data;
        public Success(T data) { this.data = data; }
        public T getData() { return data; }
    }

    public static final class Error<T> extends Resource<T> {
        private final String message;
        public Error(String message) { this.message = message; }
        public String getMessage() { return message; }
    }
}