package com.rama.cloudstorage.util;

@FunctionalInterface
public interface MinioAction<T> {
    T execute() throws Exception;
}
