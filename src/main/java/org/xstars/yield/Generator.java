package org.xstars.yield;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * 表示一个生成器，可实现迭代惰性求值。
 * 
 * @param <E> 生成器的生成的元素的类型。
 */
public abstract class Generator<E>
implements AutoCloseable, Cloneable, Runnable, Iterable<E>, Iterator<E> {
    /**
     * 指示生成线程是否已经启动。
     */
    private volatile boolean isStarted;

    /**
     * 指示当前元素是否存在，也即生成器是否未停止。
     */
    private volatile boolean hasCurrent;

    /**
     * 指示生成器是否已经生成了当前元素。 用于确定 {@link #hasNext()} 和 {@link #next()} 方法的行为。
     */
    private volatile boolean hasMoved;

    /**
     * 生成器当前状态对应生成的元素。
     */
    private volatile E current;

    /**
     * 生成元素的线程。
     */
    private volatile Thread yielding;

    /**
     * 初始化生成器的新实例。
     */
    public Generator() {
        this.reset();
    }

    /**
     * 在回收当前实例前调用，执行清理操作。
     */
    @Override
    protected void finalize() throws Throwable {
        this.close();
        super.finalize();
    }

    /**
     * 创建当前生成器的副本，并重设副本的状态。
     */
    @Override
    @SuppressWarnings("unchecked")
    public Generator<E> clone() {
        try {
            Generator<E> clone = (Generator<E>) super.clone();
            clone.reset();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    /**
     * 返回当前生成器对应的迭代器对象。
     * 
     * @return 当前生成器对应的迭代器对象。
     */
    @Override
    public final Iterator<E> iterator() {
        return this;
    }

    /**
     * 指示生成器是否还有未生成的元素。
     * 
     * @return 若生成器还有未生成的元素，则为 {@code true}；否则为 {@code false}。
     */
    @Override
    public final boolean hasNext() {
        if (!this.hasMoved) {
            this.moveNext();
        }
        return this.hasCurrent;
    }

    /**
     * 返回生成器的下一个元素。
     * 
     * @return 生成器的下一个元素。
     */
    @Override
    public final E next() {
        if (!this.hasMoved) {
            this.moveNext();
            if (!this.hasNext()) {
                throw new NoSuchElementException();
            }
        }
        this.hasMoved = false;
        return this.current;
    }

    /**
     * 重设当前生成器的状态。
     */
    public final void reset() {
        synchronized (this) {
            this.isStarted = false;
            this.hasCurrent = false;
            this.hasMoved = false;
            this.current = null;
            this.yielding = new Thread(this);
        }
    }

    /**
     * 停止生成过程。
     */
    public final void stop() {
        this.yielding.interrupt();
        synchronized (this) {
            this.notify();
        }
    }

    /**
     * 关闭当前实例占用的资源。
     */
    @Override
    public void close() {
        this.stop();
    }

    /**
     * 运行生成器线程。
     */
    @Override
    public final void run() {
        if (Thread.currentThread() != this.yielding) {
            throw new IllegalAccessError();
        }
        this.generate();
        synchronized (this) {
            this.notify();
        }
    }

    /**
     * 在派生类中重写，使用 {@link #yield(Object)} 方法定义生成元素的过程。
     */
    protected abstract void generate();

    /**
     * 生成一个新的元素。
     * 
     * @param e 要生成的新元素。
     */
    protected final void yield(E e) {
        if (!this.yielding.isInterrupted()) {
            this.hasCurrent = true;
            this.current = e;
            this.waitAnother();
        }
    }

    /**
     * 将生成器移动到下一个元素的位置，并返回是否成功移动。
     * 
     * @return 若生成器还有未生成的元素，则为 {@code true}；否则为 {@code false}。
     */
    private final boolean moveNext() {
        this.hasCurrent = false;
        if (!this.yielding.isInterrupted()) {
            this.checkStarted();
            this.waitAnother();
        }
        this.hasMoved = true;
        return this.hasCurrent;
    }

    /**
     * 检查并启动生成线程。
     */
    private final void checkStarted() {
        if (!this.isStarted) {
            synchronized (this) {
                if (!this.isStarted) {
                    this.yielding.start();
                    this.isStarted = true;
                }
            }
        }
    }

    /**
     * 用于 {@link #yield(Object)} 和 {@link #moveNext()} 两过程的相互等待。
     */
    private final void waitAnother() {
        synchronized (this) {
            this.notify();
            try {
                this.wait();
            } catch (InterruptedException e) {
                assert this.yielding.isInterrupted();
            }
        }
    }
}
