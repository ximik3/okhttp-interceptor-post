package com.ximik3.okhttp;

public interface Function<T, R> {
	R apply(T t);
}
