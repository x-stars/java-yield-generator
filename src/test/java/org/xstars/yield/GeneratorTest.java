package org.xstars.yield;

import org.junit.Assert;

/**
 * Java 生成器模式测试。
 */
public class GeneratorTest {
    /**
     * 测试主入口点。
     * 
     * @param args 程序启动参数。
     */
    public static void main(String[] args) {
        try (Generator<Integer> range10 = GeneratorTest.generateRange(10)) {
            int compare = 0;
            for (int test : range10) {
                if (test >= 5) {
                    break;
                }
                Assert.assertEquals(compare, test);
                System.out.println(test);
                compare++;
            }
        }
    }

    /**
     * 生成一定范围的整数序列。
     * 
     * @param max 整数序列的最大值。
     * @return 从 0 到 {@code max} - 1 的整数序列。
     */
    private static Generator<Integer> generateRange(int max) {
        return new Generator<Integer>() {
            @Override
            public void generate() {
                for (int i = 0; i < max; i++) {
                    yield(i);
                }
            }
        };
    }
}
