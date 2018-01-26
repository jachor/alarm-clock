package pl.jachor.budzik.utils

import com.google.common.base.Function
import com.google.common.util.concurrent.*
import java.util.concurrent.Executor

fun <I,O> ListenableFuture<I>.transform(func : (I) -> O) : ListenableFuture<O> {
    return Futures.transform(this, Function { func(it as I) }, MoreExecutors.directExecutor())
}

fun <I,O> ListenableFuture<I>.transform(func : (I) -> O, executor: Executor) : ListenableFuture<O> {
    return Futures.transform(this, Function { func(it as I) }, executor)
}

fun <I,O> ListenableFuture<I>.transformAsync(func : (I) -> ListenableFuture<O>) : ListenableFuture<O> {
    return Futures.transformAsync(this, AsyncFunction { func(it as I) }, MoreExecutors.directExecutor())
}

fun <I> ListenableFuture<I>.listen(onSuccess : (I) -> Unit, onFailure: (Throwable) -> Unit, executor : Executor) {
    Futures.addCallback(this, object : FutureCallback<I> {
        override fun onSuccess(result: I?) {
            onSuccess(result as I)
        }

        override fun onFailure(t: Throwable) {
            onFailure(t)
        }
    }, executor)
}