package org.xstars.yield;

import org.junit.Assert;
import org.junit.Test;

/**
 * Java 生成器模式测试。
 */
public class GeneratorTest {
    /**
     * 初始化生成器模式测试的新实例。
     */
    public GeneratorTest() {
    }

    /**
     * 测试主入口点。
     * 
     * @param args 程序启动参数。
     */
    public static void main(String[] args) {
        GeneratorTest test = new GeneratorTest();
        test.testGenerate();
    }

    /**
     * 生成一定范围的整数序列。
     * 
     * @param max 整数序列的最大值。
     * @return 从 0 到 {@code max} - 1 的整数序列。
     */
    private Generator<Integer> generateRange(int max) {
        return new Generator<Integer>() {
            @Override
            public void generate() {
                for (int i = 0; i < max; i++) {
                    yield(i);
                }
            }
        };
    }

    /**
     * 测试生成器的生成过程。
     */
    @Test
    public void testGenerate() {
        try (Generator<Integer> range10 = this.generateRange(10)) {
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
}
