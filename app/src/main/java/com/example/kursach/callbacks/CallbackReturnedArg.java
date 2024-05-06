package com.example.kursach.callbacks;

public interface CallbackReturnedArg<R, A> {
    R callback(A arg);
}
