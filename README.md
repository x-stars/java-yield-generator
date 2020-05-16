# Java `yield` 生成器

Java 生成器模式基于线程互相等待的实现，可使用 `yield` 语法实现惰性求值的迭代器模式。

## 主要类型

``` Java
package org.xstars.yield;

import java.util.Iterator;

public abstract class Generator<E>
implements AutoCloseable, Cloneable, Runnable, Iterable<E>, Iterator<E>
```

## 使用范例

### 创建生成器

``` Java
Generator<Integer> generateRange(int max) {
    return new Generator<Integer>() {
        @Override
        public void generate() {
            for (int i = 0; i < max; i++) {
                yield(i);
            }
        }
    };
}
```

### 使用生成器

``` Java
try (Generator<Integer> range10 = generateRange(10)) {
    for (int test : range10) {
        if (test >= 5) {
            break;
        }
        System.out.println(test);
    }
}
```
